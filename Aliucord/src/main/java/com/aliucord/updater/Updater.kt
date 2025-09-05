/*
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.updater

import android.content.Context

import com.aliucord.*
import com.aliucord.injector.ALIUCORD_FROM_STORAGE_KEY
import com.aliucord.utils.ReflectUtils
import com.aliucord.updater.PluginUpdater.updates
import com.aliucord.Logger.*
import com.aliucord.updater.ManagerBuild

import java.io.File
import java.io.IOException

public object Updater {
    /**
     * Compares two SemVer-style versions to determine whether a component is outdated
     *
     * component - The name of the plugin
     * version - The local version of the plugin
     * newVersion - The latest version of the plugin
     * return - Whether newVersion is newer than version
     */
     @JvmStatic
     fun isOutdated(component: String, version: String, newVersion: String): Boolean {
        try {
            var versions = version.split("\\.")
            var newVersions = newVersion.split("\\.")
            var len: Int = versions.count()
            if (len > newVersions.count()) return false
            for (i in 0 until len) {
                var newInt = newVersions[i]
                var oldInt = versions[i]
                if (newInt > oldInt) return true
                if (newInt < oldInt) return false
            }
        } catch (th: NumberFormatException) {
            PluginUpdater.logger.error(String.format("Failed to check updates for %s due to an invalid updater/manifest version", component), th)
        } catch (th: NullPointerException) {
            PluginUpdater.logger.error(String.format("Failed to check updates for %s due to an invalid updater/manifest version", component), th)
        }

        return false;
    }

    private data class AliucordData (
        public var coreVersion: String,
        public var patchesVersion: String,
        public var injectorVersion: String,
        public var versionCode: Int,
    )

    var isAliucordOutdated: Boolean? = true
    var isDiscordOutdated: Boolean? = false
    var isPatchesOutdated: Boolean? = false
    var isInjectorOutdated: Boolean? = false

    var currentPatchesVersion = ManagerBuild.metadata?.run { "$patchesVersion" } ?: ""
    var currentInjectorVersion = ManagerBuild.metadata?.run { "$injectorVersion" } ?: ""

    fun fetchAliucordData(): Boolean {
        try {
            var url = "https://raw.githubusercontent.com/Aliucord/Aliucord/builds/data.json"
            var res = Http.simpleJsonGet(url, AliucordData::class.java)
            isAliucordOutdated = isOutdated("Aliucord", BuildConfig.VERSION, res.coreVersion)
            isDiscordOutdated = Constants.DISCORD_VERSION < res.versionCode
            isPatchesOutdated = isOutdated("Patches", currentPatchesVersion, res.patchesVersion)
            isInjectorOutdated = isOutdated("Injector", currentInjectorVersion, res.injectorVersion)
            return true;
        } catch (ex: IOException) {
            PluginUpdater.logger.error("Failed to check updates for Aliucord", ex);
            return false;
        }
    }

    /**
     * Determines whether Aliucord is outdated
     *
     * return - Whether latest remote Aliucord commit hash is newer than the installed one
     */
    @JvmStatic
    public fun AliucordOutdated(): Boolean {
        if (usingDexFromStorage() || isUpdaterDisabled()) return false
        if (isAliucordOutdated == null && !fetchAliucordData()) return false
        return isAliucordOutdated!!
    }

    /**
     * Determines whether Patches is outdated
     *
     * return - Whether Aliucord's currently supported Patches version is newer than the installed one
     */
    @JvmStatic
    public fun PatchesOutdated(): Boolean {
        if (isPatchesOutdated == null && !fetchAliucordData()) return false
        return isPatchesOutdated!!
    }

    /**
     * Determines whether Injector is outdated
     *
     * return - Whether Aliucord's currently supported Injector version is newer than the installed one
     */
    @JvmStatic
    public fun InjectorOutdated(): Boolean {
        if (isInjectorOutdated == null && !fetchAliucordData()) return false
        return isInjectorOutdated!!
    }

    /**
     * Determines whether the Base Discord is outdated
     *
     * return - Whether Aliucord's currently supported Discord version is newer than the installed one
     */
    @JvmStatic
    public fun DiscordOutdated(): Boolean {
        if (isUpdaterDisabled()) return false
        if (isDiscordOutdated == null && !fetchAliucordData()) return false
        return isDiscordOutdated!!
    }


    /**
     * Replaces the local Aliucord version with the latest from Github
     *
     * param ctx - Context
     */
    @JvmStatic
    public fun updateAliucord(ctx: Context) {
        var c: Class<*>
        try {
            c = Class.forName("com.aliucord.injector.InjectorKt")
        } catch (e: ClassNotFoundException) {
            c = Class.forName("com.aliucord.injector.Injector")
        }
        ReflectUtils.invokeMethod(
            c,
            null,
            "downloadLatestAliucordDex",
            File(ctx.getCodeCacheDir(), "Aliucord.zip")
        );
    }

    /**
     * Determines whether the updater is disabled
     *
     * return - Whether preference "disableAliucordUpdater" is set to true
     */
    @JvmStatic
    public fun isUpdaterDisabled(): Boolean {
        return Main.settings.getBool("disableAliucordUpdater", false)
    }

    /**
     * Determines whether the Aliucord dex is being loaded from storage
     *
     * return - Whether preference {@link AliucordPage#ALIUCORD_FROM_STORAGE_KEY} is set to true
     */
    @JvmStatic
    public fun usingDexFromStorage(): Boolean {
        return Main.settings.getBool(ALIUCORD_FROM_STORAGE_KEY, false)
    }
}
