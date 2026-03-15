/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2022 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.injector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.discord.app.App
import com.discord.app.AppActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.json.JSONObject
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/**
 * External settings key controlling whether custom core is enabled.
 */
private const val ALIUCORD_FROM_STORAGE_KEY = "AC_from_storage"

private val applicationInitialized = AtomicBoolean(false)
private val activityInitialized = AtomicBoolean(false)

/**
 * The main entrypoint, invoked by the overridden Discord class.
 * This is invoked shortly after [App.onCreate] starts executing.
 */
internal fun init(appCtx: Application) {
    if (applicationInitialized.getAndSet(true)) return

    Logger.init()
    Logger.d("Started Aliucord Injector!")

    try {
        if (!XposedBridge.disableHiddenApiRestrictions()) {
            Logger.w("Failed to disable hidden api restrictions")
        }
        if (!XposedBridge.disableProfileSaver()) {
            Logger.w("Failed to disable profile saver")
        }

        pruneArtProfile(appCtx)
    } catch (e: Exception) {
        Logger.e("Failed to setup environment", e)
    }

    try {
        Injector(appCtx).onApplicationCreate()
    } catch (t: Throwable) {
        Logger.errorToast(appCtx, "Failed to run Aliucord Injector", t)
    }
}

private class Injector(private val appCtx: Application) {
    /**
     * Aliucord's base directory in external storage that's accessible to the user, storing settings and plugins.
     */
    private val externalBaseDir = Environment.getExternalStorageDirectory().resolve("Aliucord")

    /**
     * Aliucord's main core settings file. This is used to determine whether a custom core is enabled.
     */
    private val externalSettingsFile = externalBaseDir.resolve("settings/Aliucord.json")

    /**
     * A custom core that was built and deployed to this device, accessible to the user.
     */
    private val externalCustomCoreFile = externalBaseDir.resolve("Aliucord.zip")

    /**
     * An official Aliucord core build downloaded by Injector.
     * This is inaccessible to users and is stored in internal cache.
     */
    private val internalCoreFile = appCtx.codeCacheDir.resolve("Aliucord.zip")

    /**
     * A copy of [externalCustomCoreFile], to prevent corruption while Aliucord is already running.
     * This is inaccessible to users and stored in internal cache.
     */
    private val internalCustomCoreFile = appCtx.codeCacheDir.resolve("Aliucord.custom.zip")

    /**
     * This is invoked when [App.onCreate] is called, triggering a possible early initialization of the Aliucord core
     * if permissions have been granted and the core has already been downloaded during a prior launch.
     */
    fun onApplicationCreate() {
        if (!isPermissionsGranted()) {
            restoreCoreFlow()
            return
        }

        Logger.d("Checking custom core settings")
        val useCustomCore = isUsingCustomCore()

        // Delete old custom cores copied to code cache if they exist
        if (!useCustomCore) {
            internalCustomCoreFile.delete()
        } else {
            Logger.d("Using custom Aliucord core!")
        }

        // Copy core bundle from external storage to internal cache to prevent deletion while running
        if (useCustomCore) {
            externalCustomCoreFile.copyTo(internalCustomCoreFile, overwrite = true)
        }
        // Download new stable core
        else if (!internalCoreFile.exists()) {
            restoreCoreFlow()
            return
        }

        // Load the core
        val loadTarget = if (useCustomCore) internalCustomCoreFile else internalCoreFile
        Logger.d("Adding Aliucord core ${loadTarget.absolutePath} the classpath...")
        addDexToClasspath(
            dexFile = loadTarget,
            classLoader = appCtx.classLoader,
        )

        // Start the loaded core
        try {
            startAliucord()
        } catch (t: Throwable) {
            Logger.errorToast(appCtx, "Failed to start Aliucord!", t)

            try {
                // Delete cached files so it is redownloaded the next time
                internalCoreFile.delete()
                internalCustomCoreFile.delete()
            } catch (e: Throwable) {
                Logger.e("Failed to delete cached core files", e)
            }

            if (useCustomCore && disableCustomCore()) {
                Logger.errorToast(appCtx, "Disabled loading custom core!")
            }
        }
    }

    /**
     * The "restoration" flow of Aliucord that waits for the user to launch an activity,
     * grant permissions, and then download the core and forcefully restart the process
     * to allow for early initialization of the Aliucord core.
     */
    fun restoreCoreFlow() {
        hookActivityOnCreate { activity ->
            val permissionsResult = ::requestPermissions.takeIf { !isPermissionsGranted() }?.invoke(activity)

            Thread {
                if (permissionsResult?.get() == false)
                    return@Thread

                if (!isUsingCustomCore() && !installCore())
                    return@Thread

                restartAliucord()
            }.start()
        }
    }

    /**
     * Requests the necessary storage permissions and returns a future whether they were granted.
     * This should be called on or before [AppActivity.onCreate] before the lifecycle is RESUMED.
     */
    @SuppressLint("UseKtx")
    fun requestPermissions(activity: AppActivity): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val userMsg = "Aliucord requires the 'All files access' permission to use its folder in Internal Storage!"
            appCtx.showToast(userMsg)

            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val granted = Environment.isExternalStorageManager()
                if (!granted) {
                    appCtx.showToast(userMsg)
                    Logger.e("User did not grant MANAGE_EXTERNAL_STORAGE permission!")
                }
                future.complete(granted)
            }.launch(
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:${activity.packageName}"))
            )
        } else {
            val userMsg = "Aliucord requires storage permissions to use its folder in Internal Storage!"
            appCtx.showToast(userMsg)

            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) {
                    appCtx.showToast(userMsg)
                    Logger.e("User did not grant WRITE_EXTERNAL_STORAGE permission!")
                }
                future.complete(granted)
            }.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return future
    }

    /**
     * Performs the whole download flow of obtaining the latest core build.
     *
     * This first checks whether the current installation is compatible with the
     * latest core and prompts to reinstall with Manager when possible.
     */
    private fun installCore(): Boolean {
        return try {
            val data = fetchBuildData()
            Logger.d("Retrieved remote build data: $data")

            if (data.discordVersion > com.discord.BuildConfig.VERSION_CODE ||
                KotlinVersion.CURRENT < data.kotlinVersionParsed
            ) {
                // TODO: launch aliucord manager reinstall
                Logger.errorToast(appCtx, "Your base Discord is outdated. Please reinstall using Aliucord Manager.")
                false
            } else {
                downloadLatestCore(outputFile = internalCoreFile)
                true
            }
        } catch (e: Throwable) {
            Logger.errorToast(appCtx, "Failed to download latest Aliucord core!", e)
            false
        }
    }

    // -------- Starting Aliucord -------- //
    /**
     * Finds the correct Aliucord core entrypoint and runs it.
     */
    private fun startAliucord() {
        Logger.d("Obtaining Aliucord core entrypoints...")

        try {
            val c = Class.forName("com.aliucord.Main")
            val onApplicationInit = c.getDeclaredMethod("onApplicationInit", Application::class.java)

            Logger.d("Starting early Aliucord core...")
            onApplicationInit.invoke(null, appCtx)
            Logger.d("Finished early starting Aliucord")
        } catch (_: NoSuchMethodException) {
            startLegacyAliucord()
        }
    }

    /**
     * Finds the old Aliucord core entrypoints and runs it.
     * Old Aliucord core did not support early init, so we wait for Activity creation.
     */
    private fun startLegacyAliucord() {
        Logger.d("Obtaining Aliucord core legacy entrypoints...")
        val c = Class.forName("com.aliucord.Main")
        val preInit = c.getDeclaredMethod("preInit", AppActivity::class.java)
        val init = c.getDeclaredMethod("init", AppActivity::class.java)

        hookActivityOnCreate { activity ->
            Logger.d("Starting Aliucord core...")
            preInit.invoke(null, activity)
            init.invoke(null, activity)
            Logger.d("Finished starting Aliucord")
        }
    }

    // -------- Utilities -------- //

    /**
     * Checks whether external storage write permissions have been granted.
     */
    private fun isPermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            appCtx.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Forcefully restarts the Aliucord app process.
     */
    private fun restartAliucord(): Nothing {
        Logger.i("Force restarting Aliucord...")
        try {
            val intent = appCtx.packageManager.getLaunchIntentForPackage(appCtx.packageName)
                .let { Intent.makeRestartActivityTask(it?.component) }

            appCtx.startActivity(intent)
        } catch (e: Exception) {
            Logger.e("Failed to restart Aliucord!", e)
        } finally {
            exitProcess(0)
        }
    }

    /**
     * Checks whether using an external custom Aliucord core is possible and enabled.
     */
    private fun isUsingCustomCore(): Boolean {
        val settings = try {
            if (!externalSettingsFile.exists()) {
                Logger.d("Aliucord settings file missing, skipping custom core check...")
                return false
            }
            if (!externalCustomCoreFile.exists()) {
                Logger.d("Aliucord custom core missing, skipping custom core check...")
                return false
            }

            JSONObject(externalSettingsFile.readText())
        } catch (e: Exception) {
            // This may include permission errors
            Logger.d("Failed to read external Aliucord settings, skipping custom core check...", e)
            return false
        }

        return settings.optBoolean(ALIUCORD_FROM_STORAGE_KEY, false)
    }

    /**
     * Tries to disable the setting that enables using a custom external Aliucord core, if possible.
     * If permissions were not granted to read/write to the external settings, then this fails silently.
     */
    private fun disableCustomCore(): Boolean {
        return try {
            if (!externalSettingsFile.exists()) return true

            val settings = JSONObject(externalSettingsFile.readText())
            if (!settings.optBoolean(ALIUCORD_FROM_STORAGE_KEY, false)) {
                return true
            }

            settings.put(ALIUCORD_FROM_STORAGE_KEY, false)
            externalSettingsFile.writeText(settings.toString())
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Attempts to hook [AppActivity.onCreate] if it has not been invoked yet and run a [callback]
     * when it is executed. This allows for an unlimited amount of callbacks to be registered prior to
     * the activity initializing, however disallows hooking once it's initialized. As such, it should not
     * be used multiple times throughout different parts of the same initialization flow.
     */
    private fun hookActivityOnCreate(callback: (AppActivity) -> Unit) {
        Logger.d("Hooking AppActivity.onCreate")

        if (activityInitialized.get()) {
            throw IllegalStateException("Cannot hook AppActivity.onCreate when it was already invoked!")
        }

        try {
            var unhook: XC_MethodHook.Unhook? = null
            unhook = XposedBridge.hookMethod(
                AppActivity::class.java.getDeclaredMethod("onCreate", Bundle::class.java),
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        unhook!!.unhook()
                        activityInitialized.set(true)
                        callback(param.thisObject as AppActivity)
                    }
                },
            )
        } catch (e: Exception) {
            Logger.errorToast(appCtx, "Failed to initialize Aliuhook!", e)
            throw e
        }
    }
}
