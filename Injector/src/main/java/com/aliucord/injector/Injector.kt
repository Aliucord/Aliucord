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
import dalvik.system.BaseDexClassLoader
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

const val LOG_TAG = "Injector"
private const val DATA_URL = "https://raw.githubusercontent.com/Aliucord/Aliucord/builds/data.json"
private const val DEX_URL = "https://raw.githubusercontent.com/Aliucord/Aliucord/builds/Aliucord.zip"

@Suppress("DEPRECATION")
private val BASE_DIRECTORY = File(Environment.getExternalStorageDirectory().absolutePath, "Aliucord")
private const val ALIUCORD_FROM_STORAGE_KEY = "AC_from_storage"

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

    Logger.d("Initializing Aliucord...")

    try {
        val dexFile = appActivity.codeCacheDir.resolve("Aliucord.zip")
        val customDexFile = appActivity.codeCacheDir.resolve("Aliucord.custom.zip")
        val useCustomDex = useCustomDex(appActivity)

        // Copy core bundle from external storage to internal cache
        if (useCustomDex) {
            File(BASE_DIRECTORY, "Aliucord.zip").copyTo(customDexFile, overwrite = true)
        }

        // Download new core
        if (!useCustomDex && !dexFile.exists()) {
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
                    } else downloadLatestAliucordDex(dexFile)
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

        Logger.d("Adding Aliucord to the classpath...")
        addDexToClasspath(if (useCustomDex) customDexFile else dexFile, appActivity.classLoader)
        val c = Class.forName("com.aliucord.Main")
        val preInit = c.getDeclaredMethod("preInit", AppActivity::class.java)
        val init = c.getDeclaredMethod("init", AppActivity::class.java)

        Logger.d("Invoking main Aliucord entry point...")
        preInit.invoke(null, appActivity)
        init.invoke(null, appActivity)
        Logger.d("Finished initializing Aliucord")
    } catch (th: Throwable) {
        error(appActivity, "Failed to initialize Aliucord :(", th)
        // Delete file so it is reinstalled the next time
        try {
            File(appActivity.codeCacheDir, "Aliucord.zip").delete()
        } catch (ignored: Throwable) {
        }
    }
}

/**
 * Checks if app has permission for storage and if so checks settings as to whether
 * using custom/local core bundle is enable .
 */
private fun useCustomDex(appActivity: AppActivity): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager())
            return false
    } else if (appActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        return false
    }

    val settingsFile = File(BASE_DIRECTORY, "settings/Aliucord.json")
    val localDexFile = File(BASE_DIRECTORY, "Aliucord.zip")
    if (!settingsFile.exists() || !localDexFile.exists()) return false

    val useLocalDex = settingsFile.readText()
        .let { it.isNotEmpty() && JSONObject(it).optBoolean(ALIUCORD_FROM_STORAGE_KEY, false) }
    if (!useLocalDex) return false

    Logger.d("Loading dex from ${localDexFile.absolutePath}")
    return true
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
