/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.installer;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
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
}
