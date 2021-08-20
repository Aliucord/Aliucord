/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.updater;

import android.content.Context;

import com.aliucord.*;
import com.aliucord.utils.ReflectUtils;

import java.io.File;
import java.io.IOException;

public class Updater {
    /**
     * Compares two versions of a plugin to determine whether it is outdated
     * @param plugin The name of the plugin
     * @param version The local version of the plugin
     * @param newVersion The latest version of the plugin
     * @return Whether newVersion is newer than version
     */
    public static boolean isOutdated(String plugin, String version, String newVersion) {
        try {
            String[] versions = version.split("\\.");
            String[] newVersions = newVersion.split("\\.");
            int len = versions.length;
            if (len != newVersions.length) return false;
            for (int i = 0; i < len; i++) {
                if (Integer.parseInt(newVersions[i]) > Integer.parseInt(versions[i])) return true;
            }
        } catch (NullPointerException | NumberFormatException th) {
            PluginUpdater.logger.error(String.format("Failed to check updates for plugin %s due to an invalid updater/manifest version", plugin), th);
        }

        return false;
    }

    private static class AliucordData {
        public String aliucordHash;
        public int versionCode;
    }

    private static Boolean isAliucordOutdated = null;
    private static Boolean isDiscordOutdated = null;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean fetchAliucordData() {
        try (var req = new Http.Request("https://raw.githubusercontent.com/Aliucord/Aliucord/builds/data.json")) {
            var res = req.execute().json(AliucordData.class);
            isAliucordOutdated = !BuildConfig.GIT_REVISION.equals(res.aliucordHash);
            isDiscordOutdated = Constants.DISCORD_VERSION < res.versionCode;
            return true;
        } catch (IOException ex) {
            PluginUpdater.logger.error("Failed to check updates for Aliucord", ex);
            return false;
        }
    }

    /**
     * Determines whether Aliucord is outdated
     * @return Whether latest remote Aliucord commit hash is newer than the installed one
     */
    public static boolean isAliucordOutdated() {
        if (usingDexFromStorage() || isUpdaterDisabled()) return false;
        if (isAliucordOutdated == null && !fetchAliucordData()) return false;
        return isAliucordOutdated;
    }

    /**
     * Determines whether the Base Discord is outdated
     * @return Whether Aliucord's currently supported Discord version is newer than the installed one
     */
    public static boolean isDiscordOutdated() {
        if (isUpdaterDisabled()) return false;
        if (isDiscordOutdated == null && !fetchAliucordData()) return false;
        return isDiscordOutdated;
    }

    /**
     * Replaces the local Aliucord version with the latest from Github
     * @param ctx Context
     * @throws Throwable If an error occurred
     */
    public static void updateAliucord(Context ctx) throws Throwable {
        ReflectUtils.invokeMethod(
                Class.forName("com.aliucord.injector.Injector"),
                (Object) null,
                "downloadLatestAliucordDex",
                new File(ctx.getCodeCacheDir(), "Aliucord.zip")
        );
    }

    /**
     * Determines whether the update is outdated
     * @return Whether preference "disableAliucordUpdater" is set to true
     */
    public static boolean isUpdaterDisabled() {
        return SettingsUtils.getBool("disableAliucordUpdater", false);
    }

    /**
     * Determines whether the Aliucord dex is being loaded from storage
     * @return Whether preference {@link com.aliucord.settings.Updater.UpdaterSettings#ALIUCORD_FROM_STORAGE} is set to true
     */
    public static boolean usingDexFromStorage() {
        return SettingsUtils.getBool(com.aliucord.settings.Updater.UpdaterSettings.ALIUCORD_FROM_STORAGE, false);
    }
}
