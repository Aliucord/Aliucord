package com.aliucord.coreplugins;

import android.content.Context;

import com.aliucord.coreplugins.plugindownloader.PluginDownloader;
import com.aliucord.PluginManager;
import com.aliucord.coreplugins.welcomepage.WelcomeScreen;
import com.aliucord.entities.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/** CorePlugins Manager */
public final class CorePlugins {
    private static final Map<String, Plugin> corePlugins = new LinkedHashMap<>();

    /** Loads all core plugins */
    public static void loadAll(Context context) {
        corePlugins.put("CommandHandler", new CommandHandler());
        corePlugins.put("CoreCommands", new CoreCommands());
        corePlugins.put("NoTrack", new NoTrack());
        corePlugins.put("PluginDownloader", new PluginDownloader());
        corePlugins.put("SupportWarn", new SupportWarn());
        corePlugins.put("TokenLogin", new TokenLogin());
        corePlugins.put("WelcomePage", new WelcomeScreen());

        for (Entry<String, Plugin> entry : corePlugins.entrySet()) {
            Plugin p = entry.getValue();
            PluginManager.logger.info("Loading core plugin: " + entry.getKey());
            try {
                p.load(context);
            } catch (Throwable e) {
                PluginManager.logger.errorToast("Failed to load core plugin " + p.getName(), e);
            }
        }
    }

    /** Starts all core plugins */
    public static void startAll(Context context) {
        for (Entry<String, Plugin> entry : corePlugins.entrySet()) {
            Plugin p = entry.getValue();
            PluginManager.logger.info("Starting core plugin: " + entry.getKey());
            try {
                p.start(context);
            } catch (Throwable e) {
                PluginManager.logger.errorToast("Failed to start core plugin " + p.getName(), e);
            }
        }
    }
}
