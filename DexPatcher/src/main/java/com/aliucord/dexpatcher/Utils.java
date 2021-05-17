package com.aliucord.dexpatcher;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;

import com.aliucord.libzip.Zip;

import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Objects;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class Utils {
    public static void unzipClasses(Context context, String zipPath) throws Exception {
        File basePathFile = new File(context.getCacheDir(), "patcher");
        if (!basePathFile.exists()) basePathFile.mkdirs();
        String basePath = basePathFile.getAbsolutePath();

        Zip zip = new Zip(zipPath, 0, 'r');
        int j = zip.getTotalEntries();
        for (int i = 0; i < j; i++) {
            zip.openEntryByIndex(i);
            String name = zip.getEntryName();
            if (name.contains("classes")) zip.extractEntry(basePath + "/" + name);
            zip.closeEntry();
        }
        zip.close();
    }

    public static void baksmaliDex(Context context, File file) throws Exception {
        String outName = file.getName().split("\\.")[0];
        File out = new File(context.getCacheDir(), "patcher/"+outName);
        if (!out.exists()) out.mkdirs();
        Baksmali.disassembleDexFile(DexFileFactory.loadDexFile(file, null), out, Runtime.getRuntime().availableProcessors(), new BaksmaliOptions());
    }

    public static void copyAsset(InputStream in, File dest) throws IOException {
        OutputStream out = new FileOutputStream(dest);
        byte[] buf = new byte[4096];
        for (int read = in.read(buf); read > -1; read = in.read(buf)) out.write(buf, 0, read);
        in.close();
        out.close();
    }

    public static void copyFile(File src, File dest) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        FileChannel srcChannel = new FileInputStream(src).getChannel();
        FileChannel destChannel = new FileOutputStream(dest).getChannel();
        destChannel.transferFrom(srcChannel, 0, srcChannel.size());
        srcChannel.close();
        destChannel.close();
    }

    public static byte[] readBytes(InputStream stream) throws Exception {
        int len = stream.available();
        byte[] buf = new byte[len];
        stream.read(buf);
        stream.close();
        return buf;
    }

    public static String readFile(File file) throws Exception { return readFile(new FileInputStream(file)); }
    public static String readFile(InputStream stream) throws Exception { return new String(readBytes(stream)); }

    public static void delete(File f) {
        if (!f.exists()) return;
        if (f.isDirectory()) for (File file : Objects.requireNonNull(f.listFiles())) delete(file);
        f.delete();
    }

    public static void replaceIcon(AssetManager assets, String outApk) throws Exception {
        byte[] icon1Bytes = readBytes(assets.open("icon1.png"));
        byte[] icon2Bytes = readBytes(assets.open("icon2.png"));

        // use androguard to figure out entries
        // androguard arsc resources.arsc --id 0x7f0f0000 (icon1)
        // androguard arsc resources.arsc --id 0x7f0f0002 and androguard arsc resources.arsc --id 0x7f0f0006 (icon2)
        String[] icon1Entries = new String[]{ "MbV.png", "kbF.png", "_eu.png", "EtS.png" };
        String[] icon2Entries = new String[]{ "_h_.png", "9MB.png", "Dy7.png", "kC0.png", "oEH.png", "RG0.png", "ud_.png", "W_3.png" };

        Zip zip = new Zip(outApk, 0, 'a');
        deleteResEntries(zip, icon1Entries);
        deleteResEntries(zip, icon2Entries);

        for (String entryName : icon1Entries) writeEntry(zip, "res/" + entryName, icon1Bytes);
        for (String entryName : icon2Entries) writeEntry(zip, "res/" + entryName, icon2Bytes);
        zip.close();
    }

    private static void deleteResEntries(Zip zip, String[] entries) {
        for (String entryName : entries) zip.deleteEntry("res/" + entryName);
    }

    private static void writeEntry(Zip zip, String entryName, byte[] bytes) {
        zip.openEntry(entryName);
        zip.writeEntry(bytes, bytes.length);
        zip.closeEntry();
    }
}
