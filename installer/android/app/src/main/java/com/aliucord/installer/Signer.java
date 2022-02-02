/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.installer;

import com.aliucord.libzip.Zip;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

public final class Signer {
    public static void newKeystore(File out) throws Exception {
        char[] password = "password".toCharArray(); // TODO: make it secure; random password should be enough

        KeySet key = createKey();
        KeyStore privateKS = KeyStore.getInstance("BKS", "BC");
        privateKS.load(null, password);
        privateKS.setKeyEntry("alias", key.privateKey, password, new Certificate[]{ key.publicKey });

        privateKS.store(new FileOutputStream(out), password);
    }

    private static KeySet createKey() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();

        BigInteger serialNumber;
        do serialNumber = BigInteger.valueOf(new SecureRandom().nextInt());
        while (serialNumber.compareTo(BigInteger.ZERO) < 0);

        X500Name x500Name = new X500Name("CN=Aliucord Installer");
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
            x500Name,
            serialNumber,
            new Date(System.currentTimeMillis() - 1000L * 60L * 60L * 24L * 30L),
            new Date(System.currentTimeMillis() + 1000L * 60L * 60L * 24L * 366L * 30L),
            Locale.ENGLISH,
            x500Name,
            SubjectPublicKeyInfo.getInstance(pair.getPublic().getEncoded())
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").build(pair.getPrivate());
        return new KeySet(new JcaX509CertificateConverter().getCertificate(builder.build(signer)), pair.getPrivate());
    }

    private static final Pattern stripPattern = Pattern.compile("^META-INF/(.*)[.](MF|SF|RSA|DSA)$");

    // based on https://gist.github.com/mmuszkow/10288441
    // and https://github.com/fornwall/apksigner/blob/master/src/main/java/net/fornwall/apksigner/ZipSigner.java
    public static void signApk(File apkFile) throws Exception {
        File ks = new File(apkFile.getParent(), "ks.keystore");
        if (!ks.exists()) newKeystore(ks);
        char[] password = "password".toCharArray();
        KeyStore keyStore = KeyStore.getInstance("BKS", "BC");
        try (FileInputStream fis = new FileInputStream(ks)) {
            keyStore.load(fis, null);
        }
        String alias = keyStore.aliases().nextElement();
        KeySet keySet = new KeySet((X509Certificate) keyStore.getCertificate(alias), (PrivateKey) keyStore.getKey(alias, password));

        Zip zip = new Zip(apkFile.getAbsolutePath(), 6, 'r');

        MessageDigest dig = MessageDigest.getInstance("SHA1");
        Map<String, String> digests = new LinkedHashMap<>();

        List<String> filesToRemove = new ArrayList<>();
        int j = zip.getTotalEntries();
        for (int i = 0; i < j; i++) {
            zip.openEntryByIndex(i);
            String name = zip.getEntryName();
            if (stripPattern.matcher(name).matches()) filesToRemove.add(name);
            else digests.put(name, toBase64(dig.digest(zip.readEntry())));
            zip.closeEntry();
        }
        zip.close();
        zip = new Zip(apkFile.getAbsolutePath(), 6, 'a');

        for (String name : filesToRemove) zip.deleteEntry(name);

        Map<String, String> sectionDigests = new LinkedHashMap<>();

        Manifest manifest = new Manifest();
        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(new Attributes.Name("Created-By"), "Aliucord Installer");

        Attributes.Name digestAttr = new Attributes.Name("SHA1-Digest");
        for (Map.Entry<String, String> entry : digests.entrySet()) {
            Attributes attributes = new Attributes();
            attributes.put(digestAttr, entry.getValue());
            String name = entry.getKey();
            manifest.getEntries().put(name, attributes);
            sectionDigests.put(name, hashEntrySection(name, attributes, dig));
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            manifest.write(baos);
            zip.openEntry(JarFile.MANIFEST_NAME);
            zip.writeEntry(baos.toByteArray(), baos.size());
            zip.closeEntry();
        }

        String manifestHash = getManifestHash(manifest, dig);
        Manifest tmpManifest = new Manifest();
        tmpManifest.getMainAttributes().putAll(attrs);
        String manifestMainHash = getManifestHash(tmpManifest, dig);

        manifest = new Manifest();
        attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.SIGNATURE_VERSION, "1.0");
        attrs.put(new Attributes.Name("Created-By"), "Aliucord Installer");
        attrs.put(new Attributes.Name("SHA1-Digest-Manifest"), manifestHash);
        attrs.put(new Attributes.Name("SHA1-Digest-Manifest-Main-Attributes"), manifestMainHash);

        for (Map.Entry<String, String> entry : sectionDigests.entrySet()) {
            Attributes attributes = new Attributes();
            attributes.put(digestAttr, entry.getValue());
            manifest.getEntries().put(entry.getKey(), attributes);
        }
        byte[] sigBytes;
        try (ByteArrayOutputStream sigStream = new ByteArrayOutputStream()) {
            manifest.write(sigStream);
            sigBytes = sigStream.toByteArray();
            zip.openEntry("META-INF/CERT.SF");
            zip.writeEntry(sigBytes, sigStream.size());
            zip.closeEntry();
        }

        byte[] signature = signSigFile(keySet, sigBytes);
        zip.openEntry("META-INF/CERT.RSA");
        zip.writeEntry(signature, signature.length);
        zip.closeEntry();

        zip.close();
    }

    private static String hashEntrySection(String name, Attributes attrs, MessageDigest dig) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            manifest.write(baos);
            int emptyLen = baos.toByteArray().length;
            manifest.getEntries().put(name, attrs);
            baos.reset();
            manifest.write(baos);
            byte[] ob = baos.toByteArray();
            ob = Arrays.copyOfRange(ob, emptyLen, ob.length);
            return toBase64(dig.digest(ob));
        }
    }

    private static String getManifestHash(Manifest manifest, MessageDigest dig) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            manifest.write(baos);
            return toBase64(dig.digest(baos.toByteArray()));
        }
    }

    private static byte[] signSigFile(KeySet keySet, byte[] content) throws Exception {
        CMSTypedData msg = new CMSProcessableByteArray(content);
        JcaCertStore certs = new JcaCertStore(Collections.singletonList(keySet.publicKey));
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

        JcaContentSignerBuilder jcaContentSignerBuilder = new JcaContentSignerBuilder("SHA1withRSA");
        ContentSigner sha1Signer = jcaContentSignerBuilder.build(keySet.privateKey);
        JcaDigestCalculatorProviderBuilder jcaDigestCalculatorProviderBuilder = new JcaDigestCalculatorProviderBuilder();
        DigestCalculatorProvider digestCalculatorProvider = jcaDigestCalculatorProviderBuilder.build();

        JcaSignerInfoGeneratorBuilder jcaSignerInfoGeneratorBuilder = new JcaSignerInfoGeneratorBuilder(digestCalculatorProvider);
        jcaSignerInfoGeneratorBuilder.setDirectSignature(true);
        SignerInfoGenerator signerInfoGenerator = jcaSignerInfoGeneratorBuilder.build(sha1Signer, keySet.publicKey);

        gen.addSignerInfoGenerator(signerInfoGenerator);
        gen.addCertificates(certs);

        CMSSignedData sigData = gen.generate(msg, false);
        return sigData.toASN1Structure().getEncoded("DER");
    }

    private static String toBase64(byte[] data) { return new String(Base64.encode(data)); }
}
