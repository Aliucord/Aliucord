package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.PluginManager
import com.aliucord.coreplugins.plugindownloader.PluginDownloader
import com.aliucord.entities.Plugin
import java.util.*

/** CorePlugins Manager */
object CorePlugins {
    private val corePlugins: MutableMap<String, Plugin> = LinkedHashMap()

    /** Loads all core plugins */
    @JvmStatic
    fun loadAll(context: Context?) {
        corePlugins.run {
            put("Badges", Badges())
            put("CommandHandler", CommandHandler())
            put("CoreCommands", CoreCommands())
            put("NoTrack", NoTrack())
            put("PluginDownloader", PluginDownloader())
            put("SupportWarn", SupportWarn())
            put("TokenLogin", TokenLogin())
        }

        for ((key, p) in corePlugins) {
            PluginManager.logger.info("Loading core plugin: $key")
            try {
                p.load(context)
            } catch (e: Throwable) {
                PluginManager.logger.errorToast("Failed to load core plugin " + p.name, e)
            }
        }
    }

    /** Starts all core plugins */
    @JvmStatic
    fun startAll(context: Context?) {
        for ((key, p) in corePlugins) {
            PluginManager.logger.info("Starting core plugin: $key")
            try {
                p.start(context)
            } catch (e: Throwable) {
                PluginManager.logger.errorToast("Failed to start core plugin " + p.name, e)
            }
        }
    }
}
