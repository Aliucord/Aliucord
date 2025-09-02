/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.plugindownloader

import com.aliucord.*
import java.io.File
import java.io.IOException
import com.aliucord.entities.CorePlugin

internal class PluginFile(val plugin: String) : File("${Constants.PLUGINS_PATH}/$plugin.zip") {
    val isInstalled
        get() = this.exists()

    fun install(author: String, repo: String, callback: Runnable? = null) {
        install("https://github.com/$author/$repo/raw/builds/$plugin.zip", callback)
    }

    fun install(url: String, callback: Runnable? = null) {
        Utils.threadPool.execute {
            val isReinstall = isInstalled
            if (PluginManager.plugins[plugin] is CorePlugin) {
                Utils.showToast("External plugins are not able to override built-in coreplugins!")
                throw IOException("External plugins are not able to override built-in coreplugins")
            }
            
            try {
                Http.simpleDownload(url, this)
                // Plugins are started on the main thread.
                // Post to main thread here for consistency.
                // Otherwise, plugins that create Handler instances or similar will error
                Utils.mainThread.post {
                    if (isReinstall) {
                        PluginManager.stopPlugin(plugin)
                        PluginManager.unloadPlugin(plugin)
                    }
                    PluginManager.loadPlugin(Utils.appContext, this)
                    if (PluginManager.isPluginEnabled(plugin))
                        PluginManager.startPlugin(plugin)
                    else
                        PluginManager.enablePlugin(plugin)
                    Utils.showToast("Plugin $plugin successfully ${if (isReinstall) "re" else ""}installed!")

                    if (PluginManager.plugins[plugin]?.requiresRestart() == true)
                        Utils.promptRestart()

                    callback?.let { Utils.mainThread.post(it) }
                }
            } catch (ex: IOException) {
                logger.error(ex)
                Utils.showToast("Failed to download $plugin: ${ex.message}")
                if (this.exists()) this.delete()
            }
        }
    }

    fun uninstall(callback: Runnable? = null) {
        val success = this.delete()
        Utils.showToast("${if (success) "Successfully uninstalled" else "Failed to uninstall"} $plugin")
        if (success) {
            val p = PluginManager.plugins[plugin]
            PluginManager.stopPlugin(plugin)
            PluginManager.unloadPlugin(plugin)

            if (p?.requiresRestart() == true)
                Utils.promptRestart()

            callback?.let { Utils.mainThread.post(it) }
        }
    }
}
