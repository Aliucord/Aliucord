/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.injector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import com.discord.app.AppActivity;
import com.discord.app.AppLog;
import com.discord.stores.StoreClientVersion;
import com.discord.stores.StoreStream;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import top.canyie.pine.Pine;
import top.canyie.pine.PineConfig;
import top.canyie.pine.callback.MethodHook;

public final class Injector {
    private static final String LOG_TAG = "Aliucord Injector";
    private static final String DATA_URL = "https://raw.githubusercontent.com/Aliucord/Aliucord/builds/data.json";
    private static final String DEX_URL = "https://raw.githubusercontent.com/Aliucord/Aliucord/builds/Aliucord.zip";

    private static MethodHook.Unhook unhook;

    public static void init() {
        PineConfig.debug = false;
        PineConfig.debuggable = false; // Set this to true to make Aliucord debuggable
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

    private static void error(Context ctx, String msg, Throwable th) {
        AppLog.g.e(String.format("[%s] %s", LOG_TAG, msg), th, null);
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show());
    }

    private static void init(AppActivity appActivity) {
        Log.d(LOG_TAG, "Initializing Aliucord...");
        try {
            var aliucordDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Aliucord");
            var dexFile = new File(appActivity.getCodeCacheDir(), "Aliucord.zip");

            var prefs = appActivity.getSharedPreferences("aliucord", Context.MODE_PRIVATE);
            boolean useLocalDex = prefs.getBoolean("AC_from_storage", false);
            File localDex;
            if (useLocalDex && (localDex = new File(aliucordDir, "Aliucord.zip")).exists()) {
                Log.d(LOG_TAG, "Loading dex from " + localDex.getAbsolutePath());
                try (var fis = new FileInputStream(localDex)) {
                    writeAliucordZip(fis, dexFile);
                }
            } else if (!dexFile.exists()) {
                var successRef = new AtomicBoolean(true);
                var thread = new Thread(() -> {
                    try {
                        Log.d(LOG_TAG, "Checking local Discord version...");
                        var storeClientVersionField = StoreStream.class.getDeclaredField("clientVersion");
                        storeClientVersionField.setAccessible(true);
                        var clientVersionField = StoreClientVersion.class.getDeclaredField("clientVersion");
                        clientVersionField.setAccessible(true);
                        var collector = StoreStream.Companion.access$getCollector$p(StoreStream.Companion);
                        var storeClientVersion = storeClientVersionField.get(collector);
                        var version = (int) clientVersionField.get(storeClientVersion);
                        Log.d(LOG_TAG, "Retrieved local Discord version: " + version);

                        Log.d(LOG_TAG, "Fetching latest Discord version...");
                        var conn = (HttpURLConnection) new URL(DATA_URL).openConnection();
                        var sb = new StringBuilder();
                        String ln;
                        try (var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                            while ((ln = reader.readLine()) != null) sb.append(ln);
                        }
                        var remoteVersion = Integer.parseInt(new JSONObject(new JSONTokener(sb.toString())).getString("versionCode"));
                        Log.d(LOG_TAG, "Retrieved remote Discord version: " + remoteVersion);

                        if (remoteVersion > version) {
                            error(appActivity, "Your base Discord is outdated. Please reinstall using the Installer.", null);
                            successRef.set(false);
                        } else downloadLatestAliucordDex(dexFile);
                    } catch (Throwable e) {
                        error(appActivity, "Failed to install aliucord :(", e);
                        successRef.set(false);
                    }
                });
                thread.start();
                thread.join();
                if (!successRef.get()) return;
            }

            Log.d(LOG_TAG, "Adding Aliucord to the classpath...");
            addDexToClasspath(dexFile, appActivity.getCodeCacheDir(), appActivity.getClassLoader());
            var c = Class.forName("com.aliucord.Main");
            var preInit = c.getDeclaredMethod("preInit", AppActivity.class);
            var init = c.getDeclaredMethod("init", AppActivity.class);

            Log.d(LOG_TAG, "Invoking main Aliucord entry point...");
            preInit.invoke(null, appActivity);
            init.invoke(null, appActivity);
            Log.d(LOG_TAG, "Finished initializing Aliucord");
        } catch (Throwable th) {
            error(appActivity, "Failed to initialize Aliucord :(", th);
        }
    }

    /**
     * Public so it can be manually triggered from Aliucord to update itself
     * outputFile should be new File(context.getCodeCacheDir(), "Aliucord.zip");
     */
    public static void downloadLatestAliucordDex(File outputFile) throws IOException {
        Log.d(LOG_TAG, "Downloading Aliucord.zip from " + DEX_URL + "...");
        var conn = (HttpURLConnection) new URL(DEX_URL).openConnection();
        try (var is = conn.getInputStream()) {
            writeAliucordZip(is, outputFile);
        }
        Log.d(LOG_TAG, "Finished downloading Aliucord.zip");
    }

    /** https://gist.github.com/nickcaballero/7045993 */
    @SuppressLint("DiscouragedPrivateApi") // this private api seems to be stable, thanks to facebook who use it in the facebook app
    private static void addDexToClasspath(File dex, File cacheDir, ClassLoader nativeClassLoader) throws Throwable {
        Log.d(LOG_TAG, "Adding Aliucord to the classpath...");
        var mClassLoader = new DexClassLoader(dex.getAbsolutePath(), cacheDir.getAbsolutePath(), null, nativeClassLoader);

        // https://android.googlesource.com/platform/libcore/+/58b4e5dbb06579bec9a8fc892012093b6f4fbe20/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java#59
        var pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        // https://android.googlesource.com/platform/libcore/+/58b4e5dbb06579bec9a8fc892012093b6f4fbe20/dalvik/src/main/java/dalvik/system/DexPathList.java#71
        // "Should be called pathElements, but the Facebook app uses reflection to modify 'dexElements' (http://b/7726934)." LOL
        var dexElementsField = Class.forName("dalvik.system.DexPathList").getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);

        var nativeClassLoaderPathList = pathListField.get(nativeClassLoader);

        var dexElements1 = (Object[]) dexElementsField.get(nativeClassLoaderPathList);
        var dexElements2 = (Object[]) dexElementsField.get(pathListField.get(mClassLoader));
        int dexElements1Size = dexElements1.length;
        int dexElements2Size = dexElements2.length;

        var joinedDexElements = (Object[]) Array.newInstance(dexElements1.getClass().getComponentType(), dexElements1Size + dexElements2Size);
        System.arraycopy(dexElements1, 0, joinedDexElements, 0, dexElements1Size);
        System.arraycopy(dexElements2, 0, joinedDexElements, dexElements1Size, dexElements2Size);

        dexElementsField.set(nativeClassLoaderPathList, joinedDexElements);
        Log.d(LOG_TAG, "Successfully added Aliucord to the classpath");
    }

    private static void writeAliucordZip(InputStream is, File outputFile) throws IOException {
        try (var fos = new FileOutputStream(outputFile)) {
            int n;
            final int sixteenKB = 16384;
            byte[] buf = new byte[sixteenKB];
            while ((n = is.read(buf)) > -1) {
                fos.write(buf, 0, n);
            }
            fos.flush();
        }
    }
}