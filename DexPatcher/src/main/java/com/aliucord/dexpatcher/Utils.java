package com.aliucord.dexpatcher;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.aliucord.libzip.Zip;

import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Objects;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Utils {
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
}
