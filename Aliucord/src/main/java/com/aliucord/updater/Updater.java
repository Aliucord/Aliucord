/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.updater;

import android.content.Context;

import com.aliucord.*;
import com.aliucord.settings.AliucordPageKt;
import com.aliucord.utils.ReflectUtils;

import java.io.File;
import java.io.IOException;

public class Updater {
    /**
     * Compares two SemVer-style versions to determine whether a component is outdated
     *
     * @param component     The name of the plugin
     * @param version    The local version of the plugin
     * @param newVersion The latest version of the plugin
     * @return Whether newVersion is newer than version
     */
    public static boolean isOutdated(String component, String version, String newVersion) {
        try {
            String[] versions = version.split("\\.");
            String[] newVersions = newVersion.split("\\.");
            int len = versions.length;
            if (len > newVersions.length) return false;
            for (int i = 0; i < len; i++) {
                int newInt = Integer.parseInt(newVersions[i]);
                int oldInt = Integer.parseInt(versions[i]);
                if (newInt > oldInt) return true;
                if (newInt < oldInt) return false;
            }
        } catch (NullPointerException | NumberFormatException th) {
            PluginUpdater.logger.error(String.format("Failed to check updates for %s due to an invalid updater/manifest version", component), th);
        }

        return false;
    }

    private static class AliucordData {
        public String coreVersion;
        public int versionCode;
    }

    private static Boolean isAliucordOutdated = null;
    private static Boolean isDiscordOutdated = null;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean fetchAliucordData() {
        try (var req = new Http.Request("https://builds.aliucord.com/data.json")) {
            var res = req.execute().json(AliucordData.class);
            isAliucordOutdated = isOutdated("Aliucord", BuildConfig.VERSION, res.coreVersion);
            isDiscordOutdated = Constants.DISCORD_VERSION < res.versionCode;
            return true;
        } catch (IOException ex) {
            PluginUpdater.logger.error("Failed to check updates for Aliucord", ex);
            return false;
        }
    }

    /**
     * Determines whether Aliucord is outdated
     *
     * @return Whether latest remote Aliucord commit hash is newer than the installed one
     */
    public static boolean isAliucordOutdated() {
        if (usingDexFromStorage() || isUpdaterDisabled()) return false;
        if (isAliucordOutdated == null && !fetchAliucordData()) return false;
        return isAliucordOutdated;
    }

    /**
     * Determines whether the Base Discord is outdated
     *
     * @return Whether Aliucord's currently supported Discord version is newer than the installed one
     */
    public static boolean isDiscordOutdated() {
        if (isUpdaterDisabled()) return false;
        if (isDiscordOutdated == null && !fetchAliucordData()) return false;
        return isDiscordOutdated;
    }

    /**
     * Replaces the local Aliucord version with the latest from Github
     *
     * @param ctx Context
     * @throws Throwable If an error occurred
     */
    public static void updateAliucord(Context ctx) throws Throwable {
        Class<?> c;
        try {
            c = Class.forName("com.aliucord.injector.InjectorKt");
        } catch (ClassNotFoundException e) {
            c = Class.forName("com.aliucord.injector.Injector");
        }
        ReflectUtils.invokeMethod(
            c,
            (Object) null,
            "downloadLatestAliucordDex",
            new File(ctx.getCodeCacheDir(), "Aliucord.zip")
        );
    }

    /**
     * Determines whether the updater is disabled
     *
     * @return Whether preference "disableAliucordUpdater" is set to true
     */
    public static boolean isUpdaterDisabled() {
        return Main.settings.getBool("disableAliucordUpdater", false);
    }

    /**
     * Determines whether the Aliucord dex is being loaded from storage
     *
     * @return Whether preference {@link AliucordPageKt#ALIUCORD_FROM_STORAGE_KEY} is set to true
     */
    public static boolean usingDexFromStorage() {
        return Main.settings.getBool(AliucordPageKt.ALIUCORD_FROM_STORAGE_KEY, false);
    }
}
