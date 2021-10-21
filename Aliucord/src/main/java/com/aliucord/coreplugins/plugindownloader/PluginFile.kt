/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.plugindownloader

import com.aliucord.Constants
import com.aliucord.Http
import com.aliucord.PluginManager
import com.aliucord.Utils
import java.io.File
import java.io.IOException

internal class PluginFile(val plugin: String): File("${Constants.PLUGINS_PATH}/$plugin.zip") {
    val isInstalled
        get() = this.exists()

    fun install(author: String, repo: String, callback: Runnable? = null) {
        Utils.threadPool.execute {
            val isReinstall = isInstalled

            val url = "https://github.com/$author/$repo/raw/builds/$plugin.zip"
            try {
                Http.Request(url).execute().let { res ->
                    res.saveToFile(this)
                    if (isReinstall) {
                        PluginManager.stopPlugin(plugin)
                        PluginManager.unloadPlugin(plugin)
                    }
                    PluginManager.loadPlugin(Utils.appContext, this)
                    PluginManager.startPlugin(plugin)
                    Utils.showToast("Plugin $plugin successfully ${if (isReinstall) "re" else ""}installed!")
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
            PluginManager.stopPlugin(plugin)
            PluginManager.unloadPlugin(plugin)
            callback?.let { Utils.mainThread.post(it) }
        }
    }
}