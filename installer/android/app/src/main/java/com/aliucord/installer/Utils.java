/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.installer;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;

import com.aliucord.libzip.Zip;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public final class Utils {
    public static Map<String, Object> appInfoToMap(PackageManager pm, ApplicationInfo info, boolean includeIcon) {
        Map<String, Object> map = new HashMap<>();
        map.put("apkPath", info.publicSourceDir);
        map.put("packageName", info.packageName);
        map.put("name", pm.getApplicationLabel(info));

        try {
            PackageInfo pInfo = pm.getPackageInfo(info.packageName, 0);
            map.put("versionCode", pInfo.versionCode);
            map.put("versionName", pInfo.versionName);
        } catch (Throwable ignored) {}

        if (includeIcon) map.put("icon", bitmapToBase64(drawableToBitmap(info.loadIcon(pm))));

        return map;
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static String stackTraceToString(StackTraceElement[] a) {
        StringBuilder stacktrace = new StringBuilder();
        for (StackTraceElement el : a) stacktrace.append("\n   at ").append(el.toString());
        return stacktrace.toString();
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static byte[] readBytes(InputStream stream) throws Exception {
        int len = stream.available();
        byte[] buf = new byte[len];
        stream.read(buf);
        stream.close();
        return buf;
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

    public static void writeEntry(Zip zip, String entryName, byte[] bytes) {
        zip.openEntry(entryName);
        zip.writeEntry(bytes, bytes.length);
        zip.closeEntry();
    }
}
