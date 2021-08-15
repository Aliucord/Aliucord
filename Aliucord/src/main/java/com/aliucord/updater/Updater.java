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
    }

    private static Boolean isOutdated = null;
    public static boolean isAliucordOutdated() {
        if (usingDexFromStorage() || isUpdaterDisabled()) return false;
        if (isOutdated == null) {
            try (var req = new Http.Request("https://raw.githubusercontent.com/Aliucord/Aliucord/builds/data.json")) {
                var aliucordHash = req.execute().json(AliucordData.class).aliucordHash;
                isOutdated = !BuildConfig.GIT_REVISION.equals(aliucordHash);
            } catch (IOException ex) {
                PluginUpdater.logger.error("Failed to check updates for Aliucord", ex);
                return false;
            }
        }
        return isOutdated;
    }

    public static void updateAliucord(Context ctx) throws Throwable {
        ReflectUtils.invokeMethod(
                Class.forName("com.aliucord.injector.Injector"),
                (Object) null,
                "downloadLatestAliucordDex",
                new File(ctx.getCodeCacheDir(), "Aliucord.zip")
        );
    }

    public static boolean isUpdaterDisabled() {
        return SettingsUtils.getBool("disableAliucordUpdater", false);
    }

    public static boolean usingDexFromStorage() {
        return SettingsUtils.getBool(com.aliucord.settings.Updater.UpdaterSettings.ALIUCORD_FROM_STORAGE, false);
    }
}
