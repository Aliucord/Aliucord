/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.updater;

import android.text.TextUtils;

import com.aliucord.*;
import com.aliucord.api.NotificationsAPI;
import com.aliucord.entities.NotificationData;
import com.aliucord.entities.Plugin;
import com.aliucord.settings.Updater.UpdaterSettings;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;

import kotlin.Unit;

public class PluginUpdater {
    public static class UpdateInfo {
        public int minimumDiscordVersion;
        public String version;
        public String build;
        public String changelog;
        public String changelogMedia;
    }

    public static class CachedData {
        public Map<String, UpdateInfo> data;
        public long time = System.currentTimeMillis();

        public CachedData(Map<String, UpdateInfo> d) { data = d; }
    }

    public static final Logger logger = new Logger("Updater");

    public static final Map<String, CachedData> cache = new HashMap<>();
    public static final Map<String, String> updated = new HashMap<>();
    public static final List<String> updates = new ArrayList<>();

    private static final Type resType = TypeToken.getParameterized(Map.class, String.class, UpdateInfo.class).getType();

    public static void checkUpdates(boolean notify) {
        updates.clear();
        for (Map.Entry<String, Plugin> plugin : PluginManager.plugins.entrySet()) {
            if (checkPluginUpdate(plugin.getValue()))
                updates.add(plugin.getKey());
        }
        if (!notify || (updates.size() == 0 && !(!Updater.usingDexFromStorage() && Updater.isAliucordOutdated()))) return;

        NotificationData notificationData = new NotificationData()
                .setTitle("Updater")
                .setAutoDismissPeriodSecs(10)
                .setOnClick(view -> {
                    Utils.openPage(Utils.appActivity, com.aliucord.settings.Updater.class);
                    return Unit.a;
                });

        String updatablePlugins = String.format("**%s**", TextUtils.join("**, **", updates.toArray()));
        String body;
        if (SettingsUtils.getBool(UpdaterSettings.AUTO_UPDATE_PLUGINS_KEY, false)) {
            int res = PluginUpdater.updateAll();
            if (res == 0) return;
            if (res == -1) {
                body = "Something went wrong while auto updating plugins. Check the debug log for more info.";
            } else {
                body = String.format("Automatically updated %s: %s", Utils.pluralise(res, "plugin"), updatablePlugins);
            }
        } else if (updates.size() != 0) {
            body = "Updates for plugins are available: " + updatablePlugins;
        } else body = "All plugins up to date!";

        if (!Updater.usingDexFromStorage() && Updater.isAliucordOutdated()) {
            if (SettingsUtils.getBool(UpdaterSettings.AUTO_UPDATE_ALIUCORD_KEY, false)) {
                try {
                    Updater.updateAliucord(Utils.appActivity);
                    body = "Auto updated Aliucord. Please restart Aliucord to load the update - " + body;
                } catch (Throwable th) {
                    body = "Failed to auto update Aliucord. Please update it manually - " + body;
                }
            } else {
                body = "Your Aliucord is outdated. Please update it to the latest version - " + body;
            }
        }

        notificationData.setBody(Utils.renderMD(body));
        NotificationsAPI.display(notificationData);
    }

    public static boolean checkPluginUpdate(Plugin plugin) {
        Plugin.Manifest manifest = plugin.getManifest();
        if (manifest.updateUrl == null || manifest.updateUrl.equals("")) return false;

        try {
            UpdateInfo updateInfo = getUpdateInfo(plugin);
            if (updateInfo == null || updateInfo.minimumDiscordVersion > Constants.DISCORD_VERSION) return false;

            String updatedVer = updated.get(plugin.getClass().getSimpleName());
            if (updatedVer != null && !Updater.isOutdated(plugin.name, updateInfo.version, updatedVer)) return false;

            return Updater.isOutdated(plugin.name, manifest.version, updateInfo.version);
        } catch (Throwable e) { logger.error("Failed to check update for: " + plugin.getClass().getSimpleName(), e); }
        return false;
    }

    public static UpdateInfo getUpdateInfo(Plugin plugin) throws Exception {
        Plugin.Manifest manifest = plugin.getManifest();
        if (manifest.updateUrl == null || manifest.updateUrl.equals("")) return null;
        String name = plugin.getClass().getSimpleName();

        CachedData cached = cache.get(manifest.updateUrl);
        if (cached != null && cached.time > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30)) {
            UpdateInfo updateInfo = cached.data.get(name);
            UpdateInfo defaultInfo = cached.data.get("default");
            if (updateInfo == null) return defaultInfo;
            addDefaultInfo(updateInfo, defaultInfo);
            return updateInfo;
        }

        Map<String, UpdateInfo> res = Http.simpleJsonGet(manifest.updateUrl, resType);
        if (res == null) return null;
        cache.put(manifest.updateUrl, new CachedData(res));
        UpdateInfo updateInfo = res.get(name);
        UpdateInfo defaultInfo = res.get("default");
        if (updateInfo == null) return defaultInfo;
        addDefaultInfo(updateInfo, defaultInfo);
        return updateInfo;
    }

    public static int updateAll() {
        int updateCount = 0;
        for (String plugin : updates) {
            try {
                if (update(plugin) && updateCount != -1) updateCount++;
            } catch (Throwable t) {
                logger.error("Error while updating plugin " + plugin, t);
                updateCount = -1;
            }
        }
        updates.clear();
        checkUpdates(false);
        return updateCount;
    }

    public static boolean update(String plugin) throws Throwable {
        Plugin p = PluginManager.plugins.get(plugin);
        assert p != null;
        UpdateInfo updateInfo = getUpdateInfo(p);
        if (updateInfo == null) return false;

        String url = updateInfo.build;
        if (url.contains("%s")) url = String.format(url, plugin);

        try (FileOutputStream out = new FileOutputStream(Constants.BASE_PATH + "/plugins/" + p.__filename + ".zip")) {
            new Http.Request(url).execute().pipe(out);
        }

        updated.put(plugin, updateInfo.version);
        return true;
    }

    private static void addDefaultInfo(UpdateInfo updateInfo, UpdateInfo defaultInfo) {
        if (defaultInfo != null) {
            if (updateInfo.minimumDiscordVersion == 0) updateInfo.minimumDiscordVersion = defaultInfo.minimumDiscordVersion;
            if (updateInfo.version == null) updateInfo.version = defaultInfo.version;
            if (updateInfo.build == null) updateInfo.build = defaultInfo.build;
        }
    }
}
