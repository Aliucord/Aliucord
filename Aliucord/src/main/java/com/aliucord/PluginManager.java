/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.aliucord.entities.Plugin;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

import dalvik.system.PathClassLoader;

/** Aliucord's Plugin Manager */
public class PluginManager {
    /** Map containing all loaded plugins */
    public static final Map<String, Plugin> plugins = new HashMap<>();
    public static final Logger logger = new Logger("PM");

    /**
     * Loads a plugin
     * @param context Context
     * @param file Plugin file
     */
    @SuppressWarnings({"JavaReflectionMemberAccess", "unchecked"})
    public static void loadPlugin(Context context, File file) {
        String name = file.getName().replace(".zip", "").replace(".aliu", "");
        logger.info("Loading plugin: " + name);
        try {
            PathClassLoader loader = new PathClassLoader(file.getAbsolutePath(), context.getClassLoader());
            InputStream stream = loader.getResourceAsStream("ac-plugin");
            String pName = stream == null ? name : new String(Utils.readBytes(stream));
            if (plugins.containsKey(pName)) {
                logger.warn("Plugin with name " + pName + " already exists");
                return;
            }
            Class<? extends Plugin> plugin = (Class<? extends Plugin>) loader.loadClass("com.aliucord.plugins." + pName);
            Plugin p = plugin.newInstance();
            p.__filename = name;
            //noinspection ConstantConditions
            if (p.getManifest() == null) {
                logger.error(context, "Invalid manifest for plugin: " + pName, null);
                return;
            }
            if (p.needsResources) {
                // based on https://stackoverflow.com/questions/7483568/dynamic-resource-loading-from-other-apk
                AssetManager assets = AssetManager.class.newInstance();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assets, file.getAbsolutePath());
                p.resources = new Resources(assets, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
            }
            plugins.put(pName, p);
            p.load(context);
        } catch (Throwable e) { logger.error(context, "Failed to load plugin: " + name, e); }
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
        return isPluginEnabled(Utils.getMapKey(plugins, plugin));
    }
}
