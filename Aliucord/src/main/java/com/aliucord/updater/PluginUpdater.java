package com.aliucord.updater;

import android.text.TextUtils;

import com.aliucord.Constants;
import com.aliucord.HttpUtils;
import com.aliucord.Logger;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.api.NotificationsAPI;
import com.aliucord.entities.NotificationData;
import com.aliucord.entities.Plugin;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;

import kotlin.Unit;

public class PluginUpdater {
    public static class UpdateInfo {
        public int minimumDiscordVersion;
        public String version;
        public String build;
    }

    public static class CachedData {
        public Map<String, UpdateInfo> data;
        public long time = System.currentTimeMillis();

        public CachedData(Map<String, UpdateInfo> d) { data = d; }
    }

    public static final Logger logger = new Logger("PluginUpdater");

    public static final Map<String, CachedData> cache = new HashMap<>();
    public static final Map<String, String> updated = new HashMap<>();
    public static final List<String> updates = new ArrayList<>();

    private static final Type resType = TypeToken.getParameterized(Map.class, String.class, UpdateInfo.class).getType();

    public static void checkUpdates(boolean notif) {
        updates.clear();
        for (Map.Entry<String, Plugin> plugin : PluginManager.plugins.entrySet()) {
            if (checkPluginUpdate(plugin.getValue())) updates.add(plugin.getKey());
        }
        if (!notif || updates.size() == 0) return;
        NotificationData notificationData = new NotificationData();
        notificationData.title = "Updater";
        notificationData.body = Utils.renderMD("Updates for plugins are available: **" + TextUtils.join("**, **", updates.toArray()) + "**");
        notificationData.autoDismissPeriodSecs = 10;
        notificationData.onClick = v -> {
            Utils.openPage(v.getContext(), com.aliucord.settings.Updater.class);
            return Unit.a;
        };
        NotificationsAPI.display(notificationData);
    }

    public static boolean checkPluginUpdate(Plugin plugin) {
        Plugin.Manifest manifest = plugin.getManifest();
        if (manifest.updateUrl == null || manifest.updateUrl.equals("")) return false;

        try {
            UpdateInfo updateInfo = getUpdateInfo(plugin);
            if (updateInfo == null || updateInfo.minimumDiscordVersion > Constants.DISCORD_VERSION) return false;

            String updatedVer = updated.get(plugin.getClass().getSimpleName());
            if (updatedVer != null && !Updater.isOutdated(updateInfo.version, updatedVer)) return false;

            return Updater.isOutdated(manifest.version, updateInfo.version);
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

        Map<String, UpdateInfo> res = Utils.fromJson(HttpUtils.stringRequest(manifest.updateUrl, null), resType);
        if (res == null) return null;
        cache.put(manifest.updateUrl, new CachedData(res));
        UpdateInfo updateInfo = res.get(name);
        UpdateInfo defaultInfo = res.get("default");
        if (updateInfo == null) return defaultInfo;
        addDefaultInfo(updateInfo, defaultInfo);
        return updateInfo;
    }

    public static void updateAll() {
        for (String plugin : updates) update(plugin);
        updates.clear();
        checkUpdates(false);
    }

    public static void update(String plugin) {
        try {
            Plugin p = PluginManager.plugins.get(plugin);
            UpdateInfo updateInfo = getUpdateInfo(p);
            if (updateInfo == null) return;

            String url = updateInfo.build;
            if (url.contains("%s")) url = String.format(url, plugin);
            ReadableByteChannel in = Channels.newChannel(HttpUtils.request(url, null));
            FileChannel out = new FileOutputStream(Constants.BASE_PATH + "/plugins/" + p.__filename + ".apk").getChannel();
            out.transferFrom(in, 0, Long.MAX_VALUE);
            in.close();
            out.close();

            updated.put(plugin, updateInfo.version);
        } catch (Throwable e) { logger.error(e); }
    }

    private static void addDefaultInfo(UpdateInfo updateInfo, UpdateInfo defaultInfo) {
        if (defaultInfo != null) {
            if (updateInfo.minimumDiscordVersion == 0) updateInfo.minimumDiscordVersion = defaultInfo.minimumDiscordVersion;
            if (updateInfo.version == null) updateInfo.version = defaultInfo.version;
            if (updateInfo.build == null) updateInfo.build = defaultInfo.build;
        }
    }
}
