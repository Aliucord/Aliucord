/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2022 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.injector

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import com.discord.app.AppActivity
import com.discord.stores.StoreClientVersion
import com.discord.stores.StoreStream
import com.google.gson.Gson
import dalvik.system.BaseDexClassLoader
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

const val LOG_TAG = "Injector"
private const val DATA_URL = "https://builds.aliucord.com/data.json"
private const val DEX_URL = "https://builds.aliucord.com/Aliucord.zip"

private var unhook: XC_MethodHook.Unhook? = null

fun init() {
    try {
        Log.d(LOG_TAG, "Hooking AppActivity.onCreate...")
        unhook = XposedBridge.hookMethod(AppActivity::class.java.getDeclaredMethod("onCreate", Bundle::class.java), object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                init(param.thisObject as AppActivity)
                unhook!!.unhook()
                unhook = null
            }
        })
    } catch (th: Throwable) {
        Log.e(LOG_TAG, "Failed to initialize Aliucord", th)
    }
}

private fun error(ctx: Context, msg: String, th: Throwable?) {
    Logger.e(msg, th)
    Handler(Looper.getMainLooper()).post { Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show() }
}

private fun init(appActivity: AppActivity) {
    if (!XposedBridge.disableProfileSaver())
        Logger.w("Failed to disable profile saver")

    if (!XposedBridge.disableHiddenApiRestrictions())
        Logger.w("Failed to disable hidden api restrictions")

    if (!pruneArtProfile(appActivity))
        Logger.w("Failed to prune art profile")

    val internalCoreFile = appActivity.codeCacheDir.resolve("Aliucord.zip")
    val internalCustomCoreFile = appActivity.codeCacheDir.resolve("Aliucord.custom.zip")
    val externalBaseDir = Environment.getExternalStorageDirectory().resolve("Aliucord")
    val externalSettingsFile = externalBaseDir.resolve("settings/Aliucord.json")
    val externalCustomCoreFile = externalBaseDir.resolve("Aliucord.zip")

    val customCore = isUsingCustomCore(settingsFile = externalSettingsFile, customCoreFile = externalCustomCoreFile)

    Logger.d("Loading Aliucord core...")
    try {
        // Delete old custom cores copied to code cache if exist
        if (!customCore) {
            internalCustomCoreFile.delete()
        }

        // Copy core bundle from external storage to internal cache to prevent deletion while running
        if (customCore) {
            externalCustomCoreFile.copyTo(internalCustomCoreFile, overwrite = true)
        }
        // Download new stable core
        else if (!internalCoreFile.exists()) {
            val successRef = AtomicBoolean(true)
            Thread {
                try {
                    Logger.d("Checking local Discord version...")
                    val storeClientVersionField = StoreStream::class.java.getDeclaredField("clientVersion")
                        .apply { isAccessible = true }
                    val clientVersionField = StoreClientVersion::class.java.getDeclaredField("clientVersion")
                        .apply { isAccessible = true }

                    val collector = StoreStream.Companion.`access$getCollector$p`(StoreStream.Companion)
                    val storeClientVersion = storeClientVersionField[collector]
                    val version = (clientVersionField[storeClientVersion] as Int)
                    Logger.d("Retrieved local Discord version: $version")

                    Logger.d("Fetching latest Discord version...")
                    val conn = URL(DATA_URL).openConnection() as HttpURLConnection
                    val remoteVersion = JSONObject(conn.inputStream.bufferedReader().readText()).getInt("versionCode")
                    Logger.d("Retrieved remote Discord version: $remoteVersion")

                    if (remoteVersion > version) {
                        error(appActivity, "Your base Discord is outdated. Please reinstall using the Installer.", null)
                        successRef.set(false)
                    } else downloadLatestAliucordDex(internalCoreFile)
                } catch (e: Throwable) {
                    error(appActivity, "Failed to install aliucord :(", e)
                    successRef.set(false)
                }
            }.run {
                start()
                join()
            }
            if (!successRef.get()) return
        }

        Logger.d("Adding Aliucord core to the classpath...")
        addDexToClasspath(
            dex = if (customCore) internalCustomCoreFile else internalCoreFile,
            classLoader = appActivity.classLoader,
        )
        val c = Class.forName("com.aliucord.Main")
        val preInit = c.getDeclaredMethod("preInit", AppActivity::class.java)
        val init = c.getDeclaredMethod("init", AppActivity::class.java)

        Logger.d("Invoking main Aliucord entry point...")
        preInit.invoke(null, appActivity)
        init.invoke(null, appActivity)
        Logger.d("Finished initializing Aliucord")
    } catch (th: Throwable) {
        error(appActivity, "Failed to initialize Aliucord", th)
        try {
            // Delete cached files so it is redownloaded the next time
            internalCoreFile.delete()
            internalCustomCoreFile.delete()
        } catch (e: Throwable) {
            Logger.e("Failed to delete cached core files", e)
        }

        if (customCore && !disableCustomCore(externalSettingsFile)) {
            Logger.errorToast(appActivity, "Disabled loading custom core!")
        }
    }
}

/**
 * Checks whether using an external custom Aliucord core is possible and enabled.
 */
private fun isUsingCustomCore(settingsFile: File, customCoreFile: File): Boolean {
    // Explicitly checking whether external storage read permissions were granted has historically been error-prone,
    // so we can instead just capture the error if they were not granted.
    val settings = try {
        if (!settingsFile.exists()) {
            Logger.d("Aliucord settings file missing, skipping custom core check...")
            return false
        }
        if (!customCoreFile.exists()) {
            Logger.d("Aliucord external custom core missing, skipping custom core check...")
            return false
        }

        JSONObject(settingsFile.readText())
    } catch (e: Exception) {
        Logger.d("Failed to read external Aliucord settings, skipping custom core check...", e)
        return false
    }

    val customCoreEnabled = settings.optBoolean("AC_from_storage", false)
    if (customCoreEnabled)
        Logger.d("Loading external custom Aliucord core!")

    return customCoreEnabled
}

/**
 * Tries to disable the setting that enables using a custom external Aliucord core, if possible.
 * If permissions were not granted to read/write to the external settings, then this fails silently.
 */
private fun disableCustomCore(settingsFile: File): Boolean {
    try {
        if (!settingsFile.exists()) return true

        val settings = JSONObject(settingsFile.readText())
        if (!settings.optBoolean("AC_from_storage", false))
            return true

        settings.put("AC_from_storage", false)
        settingsFile.writeText(settings.toString())
        return true
    } catch (_: Exception) {
        return false
    }
}

/**
 * Public so it can be manually triggered from Aliucord to update itself
 * outputFile should be new File(context.getCodeCacheDir(), "Aliucord.zip");
 */
@Throws(IOException::class)
fun downloadLatestAliucordDex(outputFile: File) {
    Logger.d("Downloading Aliucord.zip from $DEX_URL...")
    val conn = URL(DEX_URL).openConnection() as HttpURLConnection
    conn.inputStream.use { it.copyTo(FileOutputStream(outputFile)) }
    Logger.d("Finished downloading Aliucord.zip")
}

@SuppressLint("DiscouragedPrivateApi") // this private api seems to be stable, thanks to facebook who use it in the facebook app
@Throws(Throwable::class)
private fun addDexToClasspath(dex: File, classLoader: ClassLoader) {
    Logger.d("Adding Aliucord to the classpath...")

    // https://android.googlesource.com/platform/libcore/+/58b4e5dbb06579bec9a8fc892012093b6f4fbe20/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java#59
    val pathListField = BaseDexClassLoader::class.java.getDeclaredField("pathList")
        .apply { isAccessible = true }
    val pathList = pathListField[classLoader]!!
    val addDexPath = pathList.javaClass.getDeclaredMethod("addDexPath", String::class.java, File::class.java)
        .apply { isAccessible = true }
    addDexPath.invoke(pathList, dex.absolutePath, null)
    Logger.d("Successfully added Aliucord to the classpath")
}

/**
 * Try to prevent method inlining by deleting the usage profile used by AOT compilation
 * https://source.android.com/devices/tech/dalvik/configure#how_art_works
 */
private fun pruneArtProfile(ctx: Context): Boolean {
    Logger.d("Pruning ART usage profile...")
    val profile = File("/data/misc/profiles/cur/0/" + ctx.packageName + "/primary.prof")
    if (!profile.exists()) {
        return true
    }
    if (profile.length() > 0) {
        try {
            // Delete file contents
            FileOutputStream(profile).close()
        } catch (ignored: Throwable) {
            return false
        }
    }
    return true
}
