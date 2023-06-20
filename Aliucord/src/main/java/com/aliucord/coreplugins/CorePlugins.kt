package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.PluginManager
import com.aliucord.coreplugins.plugindownloader.PluginDownloader
import com.aliucord.coreplugins.rn.RNAPI

/** CorePlugins Manager */
object CorePlugins {
    private var loaded = false
    private var started = false
    private val corePlugins = arrayOf(
        RNAPI(),
        Badges(),
        CommandHandler(),
        CoreCommands(),
        NoTrack(),
        PluginDownloader(),
        SupportWarn(),
        TokenLogin(),
        ButtonsAPI(),
        UploadSize(),
        DefaultStickers(),
        PrivateThreads()
    )

    /** Loads all core plugins */
    @JvmStatic
    fun loadAll(context: Context) {
        check(!loaded) { "CorePlugins already loaded" }
        loaded = true
        for (p in corePlugins) {
            PluginManager.logger.info("Loading core plugin: ${p.name}")
            try {
                p.load(context)
            } catch (e: Throwable) {
                PluginManager.logger.errorToast("Failed to load core plugin " + p.name, e)
            }
        }
    }

    /** Starts all core plugins */
    @JvmStatic
    fun startAll(context: Context) {
        check(!started) { "CorePlugins already started" }
        started = true
        for (p in corePlugins) {
            PluginManager.logger.info("Starting core plugin: ${p.name}")
            try {
                p.start(context)
            } catch (e: Throwable) {
                PluginManager.logger.errorToast("Failed to start core plugin " + p.name, e)
            }
        }
    }
}
