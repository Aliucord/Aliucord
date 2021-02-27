package com.aliucord;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.aliucord.coreplugins.*;
import com.aliucord.entities.Plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public class PluginManager {
    public static Map<String, Plugin> plugins = new HashMap<>();
    public static Map<String, Plugin> corePlugins = new HashMap<>();
    public static Logger logger = new Logger("PM");

    @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess"})
    public static void loadPlugin(Context context, File f) {
        String name = f.getName().replace(".apk", "");
        logger.info("Loading plugin: " + name);
        try {
            PathClassLoader loader = new PathClassLoader(f.getAbsolutePath(), context.getClassLoader());
            Class<? extends Plugin> plugin = (Class<? extends Plugin>) loader.loadClass("com.aliucord.plugins." + name);
            Plugin p = plugin.newInstance();
            if (p.getManifest() == null) {
                logger.error("Invalid manifest for plugin: " + name, null);
                return;
            }
            if (p.needsResources) {
                // based on https://stackoverflow.com/questions/7483568/dynamic-resource-loading-from-other-apk
                AssetManager assets = AssetManager.class.newInstance();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assets, f.getAbsolutePath());
                p.resources = new Resources(assets, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
            }
            plugins.put(name, p);
            p.load(context);
        } catch (Throwable e) { logger.error("Exception while loading plugin: " + name, e); }
    }

    public static void enablePlugin(String name) {
        if (isPluginEnabled(name)) return;
        SettingsUtils.setBool(getPluginPrefKey(name), true);
        try {
            startPlugin(name);
        } catch (Throwable e) { logger.error("Exception while starting plugin: " + name, e); }
    }

    public static void disablePlugin(String name) {
        if (!isPluginEnabled(name)) return;
        SettingsUtils.setBool(getPluginPrefKey(name), false);
        try {
            stopPlugin(name);
        } catch (Throwable e) { logger.error("Exception while unloading plugin: " + name, e); }
    }

    public static void togglePlugin(String name) {
        if (isPluginEnabled(name)) disablePlugin(name);
        else enablePlugin(name);
    }

    public static void startPlugin(String name) {
        logger.info("Starting plugin: " + name);
         try {
            plugins.get(name).start(Utils.getAppContext());
         } catch (Throwable e) { PluginManager.logger.error("Exception while starting plugin: " + name, e); }
    }

    public static void stopPlugin(String name) {
        logger.info("Unloading plugin: " + name);
        plugins.get(name).stop(Utils.getAppContext());
    }

    public static void loadCorePlugins(Context context) {
        corePlugins.put("CommandHandler", new CommandHandler());
        corePlugins.put("CoreCommands", new CoreCommands());
        corePlugins.put("NotificationHandler", new NotificationHandler());
        corePlugins.put("NoTrack", new NoTrack());
        corePlugins.put("TokenLogin", new TokenLogin());

        for (Map.Entry<String, Plugin> entry : corePlugins.entrySet()) {
            Plugin p = entry.getValue();
            logger.info("Loading coreplugin: " + entry.getKey());
            try {
                p.load(context);
            } catch (Throwable e) {
                logger.error("Failed to load core plugin", e);
            }
        }
    }

    public static void startCorePlugins(Context context) {
        for (Map.Entry<String, Plugin> entry : corePlugins.entrySet()) {
            Plugin p = entry.getValue();
            logger.info("Starting coreplugin: " + entry.getKey());
            try {
                p.start(context);
            } catch (Throwable e) {
                logger.error("Failed to start core plugin", e);
            }
        }
    }

    public static String getPluginPrefKey(String name) { return "AC_PM_" + name.toUpperCase(); }

    public static boolean isPluginEnabled(String name) { return SettingsUtils.getBool(getPluginPrefKey(name), true); }
    public static boolean isPluginEnabled(Plugin p) { return isPluginEnabled(Utils.getMapKey(plugins, p)); }
}
