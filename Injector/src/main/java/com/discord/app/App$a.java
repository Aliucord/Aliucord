/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.discord.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import kotlin.jvm.internal.DefaultConstructorMarker;
import top.canyie.pine.Pine;
import top.canyie.pine.PineConfig;
import top.canyie.pine.callback.MethodHook;

// This is a class by Discord which conveniently happens to be empty
// Thus it offers an amazing entry point for an injection since we can safely override the class
public final class App$a {
    private static final String LOG_TAG = "Aliucord Injector";
    private static final String DEX_URL = "https://raw.githubusercontent.com/Aliucord/Aliucord/builds/Aliucord.dex";

    private static MethodHook.Unhook unhook;

    static {
        PineConfig.debug = false;
        PineConfig.debuggable = false;
        PineConfig.disableHiddenApiPolicy = false;
        PineConfig.disableHiddenApiPolicyForPlatformDomain = false;

        try {
            Log.d(LOG_TAG, "Hooking AppActivity.onCreate...");
            unhook = Pine.hook(AppActivity.class.getDeclaredMethod("onCreate", Bundle.class), new MethodHook() {
                @Override
                public void beforeCall(Pine.CallFrame callFrame) {
                    init((AppActivity) callFrame.thisObject);
                    unhook.unhook();
                    unhook = null;
                }
            });
        } catch (Throwable th) {
            Log.e(LOG_TAG, "Failed to initialize Aliucord", th);
        }
    }

    private static void init(AppActivity appActivity) {
        Log.d(LOG_TAG, "Initializing Aliucord...");
        try {
            var aliucordDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Aliucord");
            if (!aliucordDir.exists() && !aliucordDir.mkdirs()) throw new RuntimeException("Failed to create Aliucord folder");

            var dexFile = new File(aliucordDir, "Aliucord.zip");

            Log.d(LOG_TAG, "Loading Aliucord dex...");
            addDexToClasspath(dexFile, appActivity.getCodeCacheDir(), appActivity.getClassLoader());
            var c = Class.forName("com.aliucord.Main");
            var preInit = c.getDeclaredMethod("preInit", AppActivity.class);
            var init = c.getDeclaredMethod("init", AppActivity.class);

            Log.d(LOG_TAG, "Invoking main Aliucord entry point...");
            preInit.invoke(null, appActivity);
            init.invoke(null, appActivity);
            Log.d(LOG_TAG, "Finished initializing Aliucord");
        } catch (Throwable th) {
            Log.e(LOG_TAG, "Failed to initialize Aliucord", th);
        }
    }

    /** https://gist.github.com/nickcaballero/7045993 */
    private static void addDexToClasspath(File dex, File cacheDir, ClassLoader nativeClassLoader) throws Throwable {
        Log.d(LOG_TAG, "Adding Aliucord.dex to the classpath...");
        var mClassLoader = new DexClassLoader(dex.getAbsolutePath(), cacheDir.getAbsolutePath(), null, nativeClassLoader);

        // https://android.googlesource.com/platform/libcore/+/58b4e5dbb06579bec9a8fc892012093b6f4fbe20/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java#59
        var pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        // https://android.googlesource.com/platform/libcore/+/58b4e5dbb06579bec9a8fc892012093b6f4fbe20/dalvik/src/main/java/dalvik/system/DexPathList.java#71
        // "Should be called pathElements, but the Facebook app uses reflection to modify 'dexElements' (http://b/7726934)." LOL
        var dexElementsField = Class.forName("dalvik.system.DexPathList").getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);

        var arr1 = (Object[]) dexElementsField.get(pathListField.get(mClassLoader));
        var nativeClassLoaderPathList = pathListField.get(nativeClassLoader);
        var arr2 = (Object[]) dexElementsField.get(nativeClassLoaderPathList);
        int arr1Size = arr1.length;
        int arr2Size = arr2.length;

        var joined = (Object[]) Array.newInstance(arr1.getClass().getComponentType(), arr1Size + arr2Size);
        System.arraycopy(arr1, 0, joined, 0, arr1Size);
        System.arraycopy(arr2, 0, joined, arr1Size, arr2Size);

        dexElementsField.set(nativeClassLoaderPathList, joined);
        Log.d(LOG_TAG, "Successfully added Aliucord.dex to the classpath");
    }

    private static void downloadLatestAliucordDex(AppActivity appActivity, File outputFile) throws IOException {
        var prefs = appActivity.getSharedPreferences("aliucord", Context.MODE_PRIVATE);
        if (prefs.getBoolean("dex_from_storage", false) && outputFile.exists()) {
            Log.i(LOG_TAG, "Using Aliucord dex from storage.");
            return;
        }

        Log.d(LOG_TAG, "Downloading Aliucord dex...");
        var conn = (HttpURLConnection) new URL(DEX_URL).openConnection();
        try (var is = conn.getInputStream();
             var os = new FileOutputStream(outputFile);
             var zos = new ZipOutputStream(os)) {
            zos.putNextEntry(new ZipEntry("classes.dex"));
            int n;
            byte[] buf = new byte[16384]; // 16 KB
            while ((n = is.read(buf)) > -1) {
                zos.write(buf, 0, n);
            }
            zos.closeEntry();
        }
        Log.d(LOG_TAG, "Finished downloading Aliucord dex");
    }

    public App$a(DefaultConstructorMarker defaultConstructorMarker) {}
}