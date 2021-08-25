/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.aliucord.entities.Plugin;
import com.aliucord.utils.*;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import dalvik.system.PathClassLoader;

/** Aliucord's Plugin Manager */
public class PluginManager {
    /** Map containing all loaded plugins */
    public static final Map<String, Plugin> plugins = new HashMap<>();
    public static final Map<PathClassLoader, Plugin> classLoaders = new HashMap<>();
    public static final Logger logger = new Logger("PM");

    /**
     * Loads a plugin
     * @param context Context
     * @param file Plugin file
     */
    @SuppressWarnings({"JavaReflectionMemberAccess", "unchecked"})
    public static void loadPlugin(Context context, File file) {
        String fileName = file.getName().replace(".zip", "");
        logger.info("Loading plugin: " + fileName);
        try {
            PathClassLoader loader = new PathClassLoader(file.getAbsolutePath(), context.getClassLoader());

            InputStream stream;

            Plugin.Manifest manifest;
            String name;

            Class<? extends Plugin> pluginClass;
            Plugin pluginInstance;

            if ((stream = loader.getResourceAsStream("manifest.json")) != null) {
                manifest = GsonUtils.gson.e(new InputStreamReader(stream), Plugin.Manifest.class);
                name = manifest.name;

                pluginClass = (Class<? extends Plugin>) loader.loadClass(manifest.pluginClassName);
                pluginInstance = pluginClass.newInstance();

                pluginInstance.setManifest(manifest);
                pluginInstance.name = manifest.name;
            } else if ((stream = loader.getResourceAsStream("ac-plugin")) != null) {
                name = new String(IOUtils.readBytes(stream));

                pluginClass = (Class<? extends Plugin>) loader.loadClass("com.aliucord.plugins." + name);
                pluginInstance = pluginClass.newInstance();

                manifest = pluginInstance.getManifest();

                //noinspection ConstantConditions
                if (manifest == null) {
                    logger.error(context, "Invalid manifest for plugin: " + name, null);
                    return;
                }

                manifest.name = pluginInstance.name;
                pluginInstance.setManifest(manifest);
            } else {
                logger.error(context, "No manifest found for plugin: " + fileName, null);
                return;
            }

            if (plugins.containsKey(name)) {
                logger.warn("Plugin with name " + name + " already exists");
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
        } catch (Throwable e) { logger.error(context, "Failed to load plugin: " + fileName, e); }
    }

    /**
     * Enables a loaded plugin if it isn't already enabled
     * @param name Name of the plugin to enable
     */
    public static void enablePlugin(String name) {
        if (isPluginEnabled(name)) return;
        SettingsUtils.setBool(getPluginPrefKey(name), true);
        try {
            startPlugin(name);
        } catch (Throwable e) { logger.error("Exception while starting plugin: " + name, e); }
    }

    /**
     * Disables a loaded plugin if it isn't already disables
     * @param name Name of the plugin to disable
     */
    public static void disablePlugin(String name) {
        if (!isPluginEnabled(name)) return;
        SettingsUtils.setBool(getPluginPrefKey(name), false);
        try {
            stopPlugin(name);
        } catch (Throwable e) { logger.error("Exception while unloading plugin: " + name, e); }
    }

    /**
     * Toggles a plugin. If it is enabled, it will be disabled and vice versa.
     * @param name Name of the plugin to toggle
     */
    public static void togglePlugin(String name) {
        if (isPluginEnabled(name)) disablePlugin(name);
        else enablePlugin(name);
    }

    /**
     * Starts a plugin
     * @param name Name of the plugin to start
     */
    public static void startPlugin(String name) {
        logger.info("Starting plugin: " + name);
         try {
            Objects.requireNonNull(plugins.get(name)).start(Utils.getAppContext());
         } catch (Throwable e) { logger.error("Exception while starting plugin: " + name, e); }
    }

    /**
     * Stops a plugin
     * @param name Name of the plugin to stop
     */
    public static void stopPlugin(String name) {
        logger.info("Unloading plugin: " + name);
        try {
            Objects.requireNonNull(plugins.get(name)).stop(Utils.getAppContext());
        } catch (Throwable e) { logger.error("Exception while stopping plugin " + name, e); }
    }

    /**
     * Gets the preferences key for a plugin. This is used as key for plugin settings.
     * Format: AC_PM_{PLUGIN_NAME}
     * @param name Name of the plugin
     */
    public static String getPluginPrefKey(String name) {
        return "AC_PM_" + name;
    }

    /**
     * Checks whether a plugin is enabled
     * @param name Name of the plugin
     * @return Whether the plugin is enabled
     */
    public static boolean isPluginEnabled(String name) {
        return SettingsUtils.getBool(getPluginPrefKey(name), true);
    }
    /**
     * Checks whether a plugin is enabled
     * @param plugin Plugin
     * @return Whether the plugin is enabled
     */
    @SuppressWarnings("unused")
    public static boolean isPluginEnabled(Plugin plugin) {
        return isPluginEnabled(MapUtils.getMapKey(plugins, plugin));
    }
}
