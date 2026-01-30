/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2022 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.injector

import android.app.Application
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.discord.app.AppActivity
import com.discord.stores.StoreClientVersion
import com.discord.stores.StoreStream
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

private const val DATA_URL = "https://builds.aliucord.com/data.json"
private const val CORE_URL = "https://builds.aliucord.com/Aliucord.zip"

/**
 * The main entrypoint invoked by the overridden Discord class.
 * This gets invoked as soon as Discord's [Application] class is loaded,
 * prior to [Application.onCreate] and before any activity is loaded.
 */
fun init() {
    Log.d(BuildConfig.TAG, "Loaded Aliucord injector!")
    try {
        val initialized = AtomicBoolean(false)
        var unhook: XC_MethodHook.Unhook? = null

        val activityOnCreate = AppActivity::class.java.getDeclaredMethod("onCreate", Bundle::class.java)
        unhook = XposedBridge.hookMethod(activityOnCreate, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (initialized.getAndSet(true)) return

                init(param.thisObject as AppActivity)
                unhook!!.unhook()
            }
        })
    } catch (t: Throwable) {
        Log.e(BuildConfig.TAG, "Failed to setup Injector!", t)
    }
}

/**
 * The secondary entrypoint, called after Discord's main activity is invoked and [AppActivity.onCreate] is called.
 * Aliucord is downloaded, loaded, and started at this point.
 */
private fun init(appActivity: AppActivity) {
    Logger.d("Preparing environment...")

    if (!XposedBridge.disableHiddenApiRestrictions())
        Logger.w("Failed to disable hidden api restrictions")

    if (!XposedBridge.disableProfileSaver())
        Logger.w("Failed to disable profile saver")

    Logger.d("Pruning ART usage profile...")
    pruneArtProfile(appActivity)

    val internalCoreFile = appActivity.codeCacheDir.resolve("Aliucord.zip")
    val internalCustomCoreFile = appActivity.codeCacheDir.resolve("Aliucord.custom.zip")
    val externalBaseDir = Environment.getExternalStorageDirectory().resolve("Aliucord")
    val externalSettingsFile = externalBaseDir.resolve("settings/Aliucord.json")
    val externalCustomCoreFile = externalBaseDir.resolve("Aliucord.zip")

    Logger.d("Checking custom core settings")
    val customCore = isUsingCustomCore(settingsFile = externalSettingsFile, customCoreFile = externalCustomCoreFile)

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
                        Logger.errorToast(appActivity, "Your base Discord is outdated. Please reinstall using Aliucord Manager.")
                        successRef.set(false)
                    } else {
                        downloadLatestCore(internalCoreFile)
                    }
                } catch (e: Throwable) {
                    Logger.errorToast(appActivity, "Failed to download latest Aliucord core!", e)
                    successRef.set(false)
                }
            }.run {
                start()
                join()
            }
            if (!successRef.get()) return
        }

        val loadTarget = if (customCore) internalCustomCoreFile else internalCoreFile
        Logger.d("Adding Aliucord core ${loadTarget.absolutePath} the classpath...")
        addDexToClasspath(
            dexFile = loadTarget,
            classLoader = appActivity.classLoader,
        )

        Logger.d("Obtaining Aliucord core entrypoints...")
        val c = Class.forName("com.aliucord.Main")
        val preInit = c.getDeclaredMethod("preInit", AppActivity::class.java)
        val init = c.getDeclaredMethod("init", AppActivity::class.java)

        Logger.d("Starting Aliucord core...")
        preInit.invoke(null, appActivity)
        init.invoke(null, appActivity)
        Logger.d("Finished initializing Aliucord")
    } catch (t: Throwable) {
        Logger.errorToast(appActivity, "Failed to initialize Aliucord!", t)

        try {
            // Delete cached files so it is redownloaded the next time
            internalCoreFile.delete()
            internalCustomCoreFile.delete()
        } catch (e: Throwable) {
            Logger.e("Failed to delete cached core files", e)
        }

        if (customCore && disableCustomCore(externalSettingsFile)) {
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
 * Downloads the latest Aliucord core to [outputFile].
 */
fun downloadLatestCore(outputFile: File) {
    val tmpFile = outputFile.resolveSibling(outputFile.name + ".tmp")

    Logger.d("Downloading latest Aliucord core from $CORE_URL...")
    val startTime = System.currentTimeMillis()
    val conn = URL(CORE_URL).openConnection() as HttpURLConnection

    conn.setRequestProperty("User-Agent", "Aliucord Injector/${BuildConfig.VERSION} (https://github.com/Aliucord/Aliucord)")
    conn.useCaches = false

    conn.getInputStream().use { stream ->
        tmpFile.outputStream().use { out ->
            val buffer = ByteArray(1024 * 64) // 64 KiB
            val length = conn.contentLengthLong

            var oldProgress = 0f
            var downloaded = 0
            var bytes = stream.read(buffer)
            while (bytes >= 0) {
                out.write(buffer, 0, bytes)

                val newProgress = (downloaded + bytes) / length.toFloat()
                if (newProgress >= oldProgress + 0.1f) {
                    oldProgress = newProgress
                    Logger.d(String.format(Locale.ROOT,
                        "Downloaded %.2f%% after %sms",
                        newProgress * 100,
                        System.currentTimeMillis() - startTime))
                }

                downloaded += bytes
                bytes = stream.read(buffer)
            }
        }
    }
    tmpFile.renameTo(outputFile)
    Logger.d("Downloaded Aliucord core after ${System.currentTimeMillis() - startTime}ms")
}
