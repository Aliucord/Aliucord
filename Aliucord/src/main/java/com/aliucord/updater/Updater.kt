/*
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.updater

import android.content.Context

import com.aliucord.*
import com.aliucord.injector.ALIUCORD_FROM_STORAGE_KEY
import com.aliucord.injector.downloadLatestAliucordDex

import java.io.File
import java.io.IOException

object Updater {
    /**
     * Compares two SemVer-style versions to determine whether a component is outdated
     *
     * component - The name of the plugin
     * version - The local version of the plugin
     * newVersion - The latest version of the plugin
     * return - Whether newVersion is newer than version
     */
     @JvmStatic fun isOutdated(component: String, version: String, newVersion: String): Boolean {
        try {
            val versions = version.split("\\.")
            val newVersions = newVersion.split("\\.")
            val len: Int = versions.count()
            if (len > newVersions.count()) return false
            for (i in 0 until len) {
                val newInt = newVersions[i]
                val oldInt = versions[i]
                if (newInt > oldInt) return true
                if (newInt < oldInt) return false
            }
        } catch (th: NumberFormatException) {
            PluginUpdater.logger.error(String.format("Failed to check updates for %s due to an invalid updater/manifest version", component), th)
        } catch (th: NullPointerException) {
            PluginUpdater.logger.error(String.format("Failed to check updates for %s due to an invalid updater/manifest version", component), th)
        }

        return false
    }

    // Class for the Aliucord data to be fetched
    private data class AliucordData (
        var coreVersion: String,
        var patchesVersion: String,
        var injectorVersion: String,
        var versionCode: Int,
    )

    // Set variables to null by default
    private var isAliucordOutdated: Boolean? = null
    private var isDiscordOutdated: Boolean? = null
    private var isPatchesOutdated: Boolean? = null
    private var isInjectorOutdated: Boolean? = null

    // Get the current patches and injector version using ManagerBuild
    private var currentPatchesVersion = ManagerBuild.metadata?.run { "$patchesVersion" } ?: ""
    private var currentInjectorVersion = ManagerBuild.metadata?.run { "$injectorVersion" } ?: ""

    /**
     * Fetches Aliucord data, then determines whether each component is outdated or not
     *
     * return - Whether fetching the Aliucord data was successful or not
     */
    private fun fetchAliucordData(): Boolean {
        try {
            val url = "https://raw.githubusercontent.com/Aliucord/Aliucord/builds/data.json"
            val res = Http.simpleJsonGet(url, AliucordData::class.java)
            isAliucordOutdated = isOutdated("Aliucord", BuildConfig.VERSION, res.coreVersion)
            isDiscordOutdated = Constants.DISCORD_VERSION < res.versionCode
            isPatchesOutdated = isOutdated("Patches", currentPatchesVersion, res.patchesVersion)
            isInjectorOutdated = isOutdated("Injector", currentInjectorVersion, res.injectorVersion)
            return true
        } catch (ex: IOException) {
            PluginUpdater.logger.error("Failed to check updates for Aliucord", ex)
            return false
        }
    }

    /**
     * Determines whether Aliucord is outdated
     *
     * return - Whether latest remote Aliucord commit hash is newer than the installed one
     */
    @JvmStatic fun AliucordOutdated(): Boolean {
        if (usingDexFromStorage() || isUpdaterDisabled()) return false
        if (isAliucordOutdated == null && !fetchAliucordData()) return false
        return isAliucordOutdated!!
    }

    /**
     * Determines whether Patches is outdated
     *
     * return - Whether Aliucord's currently supported Patches version is newer than the installed one
     */
    @JvmStatic fun PatchesOutdated(): Boolean {
        if (isPatchesOutdated == null && !fetchAliucordData()) return false
        return isPatchesOutdated!!
    }

    /**
     * Determines whether Injector is outdated
     *
     * return - Whether Aliucord's currently supported Injector version is newer than the installed one
     */
    @JvmStatic fun InjectorOutdated(): Boolean {
        if (isInjectorOutdated == null && !fetchAliucordData()) return false
        return isInjectorOutdated!!
    }

    /**
     * Determines whether the Base Discord is outdated
     *
     * return - Whether Aliucord's currently supported Discord version is newer than the installed one
     */
    @JvmStatic fun DiscordOutdated(): Boolean {
        if (isUpdaterDisabled()) return false
        if (isDiscordOutdated == null && !fetchAliucordData()) return false
        return isDiscordOutdated!!
    }


    /**
     * Replaces the local Aliucord version with the latest from Github
     *
     * param ctx - Context
     */
    @JvmStatic fun updateAliucord(ctx: Context) {
        downloadLatestAliucordDex(File(ctx.codeCacheDir, "Aliucord.zip"))
    }

    /**
     * Determines whether the updater is disabled
     *
     * return - Whether preference "disableAliucordUpdater" is set to true
     */
    @JvmStatic fun isUpdaterDisabled(): Boolean {
        return Main.settings.getBool("disableAliucordUpdater", false)
    }

    /**
     * Determines whether the Aliucord dex is being loaded from storage
     *
     * return - Whether preference {@link AliucordPage#ALIUCORD_FROM_STORAGE_KEY} is set to true
     */
    @JvmStatic fun usingDexFromStorage(): Boolean {
        return Main.settings.getBool(ALIUCORD_FROM_STORAGE_KEY, false)
    }
}
