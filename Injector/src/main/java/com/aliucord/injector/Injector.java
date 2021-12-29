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
import com.discord.stores.StoreClientVersion;
import com.discord.stores.StoreStream;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import dalvik.system.BaseDexClassLoader;
import top.canyie.pine.Pine;
import top.canyie.pine.PineConfig;
import top.canyie.pine.callback.MethodHook;

@SuppressWarnings({ "ResultOfMethodCallIgnored", "JavaReflectionMemberAccess" })
public final class Injector {
    public static final String LOG_TAG = "Injector";
    private static final String DATA_URL = "https://raw.githubusercontent.com/Aliucord/Aliucord/builds/data.json";
    private static final String DEX_URL = "https://raw.githubusercontent.com/Aliucord/Aliucord/builds/Aliucord.zip";
    private static final File BASE_DIRECTORY = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Aliucord");

    private static MethodHook.Unhook unhook;

    public static void init() {
        PineConfig.debug = new File(BASE_DIRECTORY, ".pine_debug").exists();
        PineConfig.debuggable = new File(BASE_DIRECTORY, ".debuggable").exists();
        Log.d(LOG_TAG, "Aliucord Debuggable: " + PineConfig.debuggable);
        PineConfig.disableHiddenApiPolicy = false;
        PineConfig.disableHiddenApiPolicyForPlatformDomain = false;
        Pine.disableJitInline();
        Pine.disableProfileSaver();
        disableHiddenApiPolicy();

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
        Logger.e(msg, th);
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show());
    }

    private static void init(AppActivity appActivity) {
        Logger.d("Initializing Aliucord...");
        if (!pruneArtProfile(appActivity))
            Logger.w("Failed to prune art profile");

        try {
            var dexFile = new File(appActivity.getCodeCacheDir(), "Aliucord.zip");

            var prefs = appActivity.getSharedPreferences("aliucord", Context.MODE_PRIVATE);
            boolean useLocalDex = prefs.getBoolean("AC_from_storage", false);
            File localDex;
            if (useLocalDex && (localDex = new File(BASE_DIRECTORY, "Aliucord.zip")).exists()) {
                Logger.d("Loading dex from " + localDex.getAbsolutePath());
                try (var fis = new FileInputStream(localDex)) {
                    writeAliucordZip(fis, dexFile);
                }
            } else if (!dexFile.exists()) {
                var successRef = new AtomicBoolean(true);
                var thread = new Thread(() -> {
                    try {
                        Logger.d("Checking local Discord version...");
                        var storeClientVersionField = StoreStream.class.getDeclaredField("clientVersion");
                        storeClientVersionField.setAccessible(true);
                        var clientVersionField = StoreClientVersion.class.getDeclaredField("clientVersion");
                        clientVersionField.setAccessible(true);
                        //noinspection AccessStaticViaInstance
                        var collector = StoreStream.Companion.access$getCollector$p(StoreStream.Companion);
                        var storeClientVersion = storeClientVersionField.get(collector);
                        var version = (Integer) clientVersionField.get(storeClientVersion);
                        assert version != null;
                        Logger.d("Retrieved local Discord version: " + version);

                        Logger.d("Fetching latest Discord version...");
                        var conn = (HttpURLConnection) new URL(DATA_URL).openConnection();
                        var sb = new StringBuilder();
                        String ln;
                        try (var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                            while ((ln = reader.readLine()) != null) sb.append(ln);
                        }
                        var remoteVersion = new JSONObject(new JSONTokener(sb.toString())).getInt("versionCode");
                        Logger.d("Retrieved remote Discord version: " + remoteVersion);

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

            Logger.d("Adding Aliucord to the classpath...");
            addDexToClasspath(dexFile, appActivity.getClassLoader());
            var c = Class.forName("com.aliucord.Main");
            var preInit = c.getDeclaredMethod("preInit", AppActivity.class);
            var init = c.getDeclaredMethod("init", AppActivity.class);

            Logger.d("Invoking main Aliucord entry point...");
            preInit.invoke(null, appActivity);
            init.invoke(null, appActivity);
            Logger.d("Finished initializing Aliucord");
        } catch (Throwable th) {
            error(appActivity, "Failed to initialize Aliucord :(", th);
            // Delete file so it is reinstalled the next time
            try {
                new File(appActivity.getCodeCacheDir(), "Aliucord.zip").delete();
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Public so it can be manually triggered from Aliucord to update itself
     * outputFile should be new File(context.getCodeCacheDir(), "Aliucord.zip");
     */
    public static void downloadLatestAliucordDex(File outputFile) throws IOException {
        Logger.d("Downloading Aliucord.zip from " + DEX_URL + "...");
        var conn = (HttpURLConnection) new URL(DEX_URL).openConnection();
        try (var is = conn.getInputStream()) {
            writeAliucordZip(is, outputFile);
        }
        Logger.d("Finished downloading Aliucord.zip");
    }

    @SuppressLint("DiscouragedPrivateApi") // this private api seems to be stable, thanks to facebook who use it in the facebook app
    private static void addDexToClasspath(File dex, ClassLoader classLoader) throws Throwable {
        Logger.d("Adding Aliucord to the classpath...");

        // https://android.googlesource.com/platform/libcore/+/58b4e5dbb06579bec9a8fc892012093b6f4fbe20/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java#59
        var pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListField.setAccessible(true);

        var pathList = pathListField.get(classLoader);
        // Android 7: https://android.googlesource.com/platform/libcore/+/refs/heads/nougat-release/dalvik/src/main/java/dalvik/system/DexPathList.java#184
        // Current latest Master: https://android.googlesource.com/platform/libcore/+/58b4e5dbb06579bec9a8fc892012093b6f4fbe20/dalvik/src/main/java/dalvik/system/DexPathList.java#214
        assert pathList != null;
        var addDexPath = pathList.getClass().getDeclaredMethod("addDexPath", String.class, File.class);
        addDexPath.setAccessible(true);
        addDexPath.invoke(pathList, dex.getAbsolutePath(), null);

        Logger.d("Successfully added Aliucord to the classpath");
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

    /**
     * Try to prevent method inlining by deleting the usage profile used by AOT compilation
     * https://source.android.com/devices/tech/dalvik/configure#how_art_works
     */
    private static boolean pruneArtProfile(Context ctx) {
        Logger.d("Pruning ART usage profile...");
        var profile = new File("/data/misc/profiles/cur/0/" + ctx.getPackageName() + "/primary.prof");

        if (!profile.exists()) {
            return false;
        }

        if (profile.length() > 0) {
            try {
                // Delete file contents
                new FileOutputStream(profile).close();
            } catch (Throwable ignored) {
                return false;
            }
        }
        return true;
    }

    /**
     * Disables the Android Hidden API by adding an exemption for everything ("L")
     * Works by getting reflection methods through reflection,
     * then invoking those methods to get and invoke a setter for hidden API exemptions.
     * This works because the VM thinks its internals calling the hidden method,
     * since you're invoking reflection methods using reflection.
     */
    private static void disableHiddenApiPolicy() {
        try {
            var mForName = Class.class.getDeclaredMethod("forName", String.class);
            var mGetDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

            var cVMRuntime = mForName.invoke(null, "dalvik.system.VMRuntime");
            var mGetRuntime = (Method) mGetDeclaredMethod.invoke(cVMRuntime, "getRuntime", null);
            Objects.requireNonNull(mGetRuntime, "Failed to get getRuntime()!");

            var mSetHiddenApiExemptions = (Method) mGetDeclaredMethod.invoke(
                cVMRuntime,
                "setHiddenApiExemptions",
                new Class[]{ String[].class }
            );
            Objects.requireNonNull(mSetHiddenApiExemptions, "Failed to get setHiddenApiExemptions()!");

            var vmRuntime = mGetRuntime.invoke(null);
            Objects.requireNonNull(vmRuntime, "Failed to get VMRuntime!");
            mSetHiddenApiExemptions.invoke(vmRuntime, (Object) new String[]{ "L" });
        } catch (Throwable t) {
            Logger.e("Failed to disable hidden api policy!", t);
        }
    }
}
