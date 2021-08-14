/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.updater;

import android.content.Context;

import com.aliucord.*;
import com.discord.app.App$a;

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

    private static class GithubApiInfo {
        public Commit commit;
        public static class Commit {
            public String message;
        }
    }

    private static Boolean isOutdated = null;
    public static boolean isAliucordOutdated() {
        if (usingDexFromStorage() || isUpdaterDisabled()) return false;
        if (isOutdated == null) {
            try (Http.Request req = new Http.Request("https://api.github.com/repos/Aliucord/Aliucord/commits/builds")) {
                String commitMsg = req.execute().json(GithubApiInfo.class).commit.message;
                isOutdated = !commitMsg.contains(BuildConfig.GIT_REVISION);
            } catch (IOException ex) {
                PluginUpdater.logger.error("Failed to check updates for Aliucord", ex);
                return false;
            }
        }
        return isOutdated;
    }

    public static void updateAliucord(Context ctx) throws Throwable {
        App$a.downloadLatestAliucordDex(new File(ctx.getCodeCacheDir(), "Aliucord.zip"));
    }

    public static boolean isUpdaterDisabled() {
        return SettingsUtils.getBool("disableAliucordUpdater", false);
    }

    public static boolean usingDexFromStorage() {
        return SettingsUtils.getBool(com.aliucord.settings.Updater.UpdaterSettings.USE_DEX_FROM_STORAGE_KEY, false);
    }
}
