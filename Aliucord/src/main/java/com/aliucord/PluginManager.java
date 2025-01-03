/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.aliucord.coreplugins.badges.SupporterBadges;
import com.aliucord.coreplugins.plugindownloader.PluginDownloader;
import com.aliucord.coreplugins.rn.RNAPI;
import com.aliucord.entities.CorePlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PreHook;
import com.aliucord.coreplugins.*;
import com.aliucord.utils.*;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;

import dalvik.system.PathClassLoader;

/** Aliucord's Plugin Manager */
public class PluginManager {
    /** Map containing all loaded plugins */
    public static final Map<String, Plugin> plugins = new LinkedHashMap<>();
    public static final Map<PathClassLoader, Plugin> classLoaders = new HashMap<>();
    public static final Logger logger = new Logger("PluginManager");
    /** Plugins that failed to load for various reasons. Map of file to String or Exception */
    public static final Map<File, Object> failedToLoad = new LinkedHashMap<>();

    /**
     * Loads a plugin
     *
     * @param context Context
     * @param file    Plugin file
     */
    @SuppressWarnings({ "JavaReflectionMemberAccess", "unchecked" })
    public static void loadPlugin(Context context, File file) {
        String fileName = file.getName().replace(".zip", "");
        logger.info("Loading plugin: " + fileName);
        try {
            var loader = new PathClassLoader(file.getAbsolutePath(), context.getClassLoader());

            Plugin.Manifest manifest;

            try (var stream = loader.getResourceAsStream("manifest.json")) {
                if (stream == null) {
                    failedToLoad.put(file, "No manifest found");
                    logger.error("Failed to load plugin " + fileName + ": No manifest found", null);
                    return;
                }

                try (var reader = new InputStreamReader(stream)) {
                    manifest = GsonUtils.fromJson(GsonUtils.getGson(), reader, Plugin.Manifest.class);
                }
            }

            var name = manifest.name;

            var pluginClass = (Class<? extends Plugin>) loader.loadClass(manifest.pluginClassName);

            Patcher.addPatch(pluginClass.getDeclaredConstructor(), new PreHook(param -> {
                var plugin = (Plugin) param.thisObject;
                try {
                    ReflectUtils.setField(Plugin.class, plugin, "manifest", manifest);
                } catch (Exception e) {
                    logger.errorToast("Failed to set manifest for " + manifest.name);
                }
            }));

            var pluginInstance = pluginClass.newInstance();
            if (plugins.containsKey(name)) {
                logger.error("Plugin with name " + name + " already exists", null);
                return;
            }

            pluginInstance.__filename = fileName;
            if (pluginInstance.needsResources) {
                // based on https://stackoverflow.com/questions/7483568/dynamic-resource-loading-from-other-apk
                AssetManager assets = AssetManager.class.newInstance();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assets, file.getAbsolutePath());
                pluginInstance.resources = new Resources(assets, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
            }
            plugins.put(name, pluginInstance);
            classLoaders.put(loader, pluginInstance);
            pluginInstance.load(context);
        } catch (Throwable e) {
            failedToLoad.put(file, e);
            logger.error("Failed to load plugin " + fileName + ":\n", e);
        }
    }

    /**
     * Unloads a plugin
     *
     * @param name Name of the plugin to unload
     */
    public static void unloadPlugin(String name) {
        logger.info("Unloading plugin: " + name);
        var plugin = plugins.get(name);

        if (plugin instanceof CorePlugin) {
            throw new IllegalArgumentException("Cannot unload coreplugin " + name);
        }

        if (plugin != null) try {
            plugin.unload(Utils.getAppContext());
            plugins.remove(name);
        } catch (Throwable e) { logger.error("Exception while unloading plugin: " + name, e); }
    }

    /**
     * Enables a loaded plugin if it isn't already enabled
     *
     * @param name Name of the plugin to enable
     */
    public static void enablePlugin(String name) {
        if (isPluginEnabled(name)) return;
        Main.settings.setBool(getPluginPrefKey(name), true);
        try {
            startPlugin(name);
        } catch (Throwable e) { logger.error("Exception while starting plugin: " + name, e); }
    }

    /**
     * Disables a loaded plugin if it isn't already disables
     *
     * @param name Name of the plugin to disable
     */
    public static void disablePlugin(String name) {
        if (!isPluginEnabled(name)) return;
        Main.settings.setBool(getPluginPrefKey(name), false);
        try {
            stopPlugin(name);
        } catch (Throwable e) { logger.error("Exception while stopping plugin: " + name, e); }
    }

    /**
     * Toggles a plugin. If it is enabled, it will be disabled and vice versa.
     *
     * @param name Name of the plugin to toggle
     */
    public static void togglePlugin(String name) {
        if (isPluginEnabled(name)) disablePlugin(name);
        else enablePlugin(name);
    }

    /**
     * Starts a plugin
     *
     * @param name Name of the plugin to start
     */
    public static void startPlugin(String name) {
        logger.info("Starting plugin: " + name);
        try {
            long startTime = System.currentTimeMillis();

            Objects.requireNonNull(plugins.get(name)).start(Utils.getAppContext());

            logger.info("Started plugin: " + name + " in " + (System.currentTimeMillis() - startTime) + " milliseconds");

        } catch (Throwable e) { logger.error("Exception while starting plugin: " + name, e); }
    }

    /**
     * Stops a plugin
     *
     * @param name Name of the plugin to stop
     */
    public static void stopPlugin(String name) {
        logger.info("Stopping plugin: " + name);
        try {
            Plugin p = plugins.get(name);

            if (p instanceof CorePlugin && ((CorePlugin) p).isRequired()) {
                throw new IllegalArgumentException("Cannot stop required coreplugin " + name);
            }

            Objects.requireNonNull(p).stop(Utils.getAppContext());
        } catch (Throwable e) { logger.error("Exception while stopping plugin " + name, e); }
    }

    /**
     * Remounts the plugin (stop -> unload -> load -> start)
     *
     * @param name Name of the plugin to remount
     */
    public static void remountPlugin(String name) {
        if (!plugins.containsKey(name)) throw new IllegalArgumentException("No such plugin: " + name);
        if (!isPluginEnabled(name)) throw new IllegalArgumentException("Plugin not enabled: " + name);
        stopPlugin(name);
        unloadPlugin(name);
        loadPlugin(Utils.getAppContext(), new File(Constants.PLUGINS_PATH, name + ".zip"));
        startPlugin(name);
    }

    /**
     * Gets the preferences key for a plugin. This is used as key for plugin settings.
     * Format: AC_PM_{PLUGIN_NAME}
     *
     * @param name Name of the plugin
     */
    public static String getPluginPrefKey(String name) {
        return "AC_PM_" + name;
    }

    /**
     * Checks whether a plugin is enabled
     *
     * @param name Name of the plugin
     * @return Whether the plugin is enabled
     */
    public static boolean isPluginEnabled(String name) {
        Plugin p = plugins.get(name);
        if (p instanceof CorePlugin && ((CorePlugin) p).isRequired()) return true;

        return Main.settings.getBool(getPluginPrefKey(name), true);
    }

    /**
     * Checks whether a plugin is enabled
     *
     * @param plugin Plugin
     * @return Whether the plugin is enabled
     */
    @SuppressWarnings("unused")
    public static boolean isPluginEnabled(Plugin plugin) {
        return isPluginEnabled(MapUtils.getMapKey(plugins, plugin));
    }

    static void loadCorePlugins(Context context) {
        CorePlugin[] corePlugins = {
            new ButtonsAPI(),
            new CommandHandler(),
            new CoreCommands(),
            new DefaultStickers(),
            new ExperimentDefaults(),
            new GifPreviewFix(),
            new MembersListFix(),
            new NoTrack(),
            new PluginDownloader(),
            new PrivateChannelsListScroll(),
            new PrivateThreads(),
            new RNAPI(),
            new Pronouns(),
            new SupportWarn(),
            new SupporterBadges(),
            new TokenLogin(),
            new UploadSize(),
            new ForwardedMessages(),
        };

        for (Plugin p : corePlugins) {
            logger.info("Loading coreplugin: " + p.getName());
            try {
                plugins.put(p.getName(), p);
                p.load(context);
            } catch (Throwable e) {
                logger.errorToast("Failed to load coreplugin " + p.getName(), e);
            }
        }
    }

    static void startCorePlugins() {
        for (Plugin p : plugins.values()) {
            if (!(p instanceof CorePlugin)) continue;
            if (!isPluginEnabled(p.getName())) continue;
            startPlugin(p.getName());
        }
    }
}
