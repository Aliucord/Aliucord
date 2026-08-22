/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2026 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.coreplugins.plugindownloader.PluginFile;
import com.aliucord.entities.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Public helpers for managing Aliucord plugins at runtime.
 */
@SuppressWarnings("unused")
public final class PluginsAPI {
    private PluginsAPI() {}

    /**
     * Gets a read-only view of all currently loaded plugins.
     *
     * @return Loaded plugin map, keyed by plugin name.
     */
    @NonNull
    public static Map<String, Plugin> getPlugins() {
        return Collections.unmodifiableMap(PluginManager.plugins);
    }

    /**
     * Gets a loaded plugin by name.
     *
     * @param name Plugin name.
     * @return The loaded plugin, or null if it is not loaded.
     */
    @Nullable
    public static Plugin getPlugin(@NonNull String name) {
        return PluginManager.plugins.get(name);
    }

    /**
     * Gets a loaded plugin by the zip filename it was loaded from.
     *
     * @param filename Plugin zip filename, with or without the {@code .zip} suffix.
     * @return The loaded plugin, or null if it is not loaded.
     */
    @Nullable
    public static Plugin getPluginByFilename(@NonNull String filename) {
        String normalizedFilename = normalizeFilename(filename);
        for (Plugin plugin : PluginManager.plugins.values()) {
            if (normalizedFilename.equals(plugin.__filename)) return plugin;
        }
        return null;
    }

    /**
     * Checks whether a plugin zip has already been loaded.
     *
     * @param filename Plugin zip filename, with or without the {@code .zip} suffix.
     * @return Whether the file is already loaded.
     */
    public static boolean isPluginFileLoaded(@NonNull String filename) {
        return getPluginByFilename(filename) != null;
    }

    /**
     * Loads every new plugin zip in Aliucord's plugins folder and starts enabled plugins.
     *
     * @return Number of newly loaded plugins.
     */
    public static int loadNewPlugins() {
        return loadNewPlugins(true);
    }

    /**
     * Loads every new plugin zip in Aliucord's plugins folder.
     *
     * @param startEnabled Whether newly loaded enabled plugins should be started immediately.
     * @return Number of newly loaded plugins.
     */
    public static int loadNewPlugins(boolean startEnabled) {
        return loadNewPlugins(Utils.getAppContext(), startEnabled);
    }

    /**
     * Loads every new plugin zip in Aliucord's plugins folder.
     *
     * @param context Context used to create plugin classloaders/resources.
     * @param startEnabled Whether newly loaded enabled plugins should be started immediately.
     * @return Number of newly loaded plugins.
     */
    public static int loadNewPlugins(@NonNull Context context, boolean startEnabled) {
        return PluginManager.loadNewPlugins(context, startEnabled);
    }

    /**
     * Loads a plugin zip from a path and starts it if enabled.
     *
     * @param path Path to a plugin zip.
     * @return Whether the plugin was loaded or was already loaded.
     */
    public static boolean loadPlugin(@NonNull String path) {
        return loadPlugin(new File(path));
    }

    /**
     * Loads a plugin zip and starts it if enabled.
     *
     * @param file Plugin zip file.
     * @return Whether the plugin was loaded or was already loaded.
     */
    public static boolean loadPlugin(@NonNull File file) {
        return loadPlugin(Utils.getAppContext(), file, true);
    }

    /**
     * Loads a plugin zip.
     *
     * @param context Context used to create plugin classloaders/resources.
     * @param file Plugin zip file.
     * @param startIfEnabled Whether the plugin should be started immediately if enabled.
     * @return Whether the plugin was loaded or was already loaded.
     */
    public static boolean loadPlugin(@NonNull Context context, @NonNull File file, boolean startIfEnabled) {
        if (!file.isFile() || !file.getName().endsWith(".zip")) return false;

        String filename = normalizeFilename(file.getName());
        Plugin plugin = getPluginByFilename(filename);
        if (plugin == null) {
            PluginManager.loadPlugin(context, file);
            plugin = getPluginByFilename(filename);
        }

        if (plugin == null) return false;
        if (startIfEnabled && PluginManager.isPluginEnabled(plugin.getName())) {
            PluginManager.startPlugin(plugin.getName());
        }
        return true;
    }

    /**
     * Downloads and installs a plugin from a URL using Aliucord's standard installer.
     *
     * @param plugin Plugin filename/name, without {@code .zip}.
     * @param url Direct URL to the plugin zip.
     */
    public static void installPlugin(@NonNull String plugin, @NonNull String url) {
        installPlugin(plugin, url, (Runnable) null);
    }

    /**
     * Downloads and installs a plugin from a URL using Aliucord's standard installer.
     *
     * @param plugin Plugin filename/name, without {@code .zip}.
     * @param url Direct URL to the plugin zip.
     * @param callback Optional callback posted on the main thread after installation finishes.
     */
    public static void installPlugin(@NonNull String plugin, @NonNull String url, @Nullable Runnable callback) {
        new PluginFile(plugin).install(url, callback);
    }

    /**
     * Downloads and installs a plugin from a GitHub plugin repository's builds branch.
     *
     * @param plugin Plugin filename/name, without {@code .zip}.
     * @param author GitHub owner.
     * @param repo GitHub repository.
     */
    public static void installPlugin(@NonNull String plugin, @NonNull String author, @NonNull String repo) {
        installPlugin(plugin, author, repo, null);
    }

    /**
     * Downloads and installs a plugin from a GitHub plugin repository's builds branch.
     *
     * @param plugin Plugin filename/name, without {@code .zip}.
     * @param author GitHub owner.
     * @param repo GitHub repository.
     * @param callback Optional callback posted on the main thread after installation finishes.
     */
    public static void installPlugin(@NonNull String plugin, @NonNull String author, @NonNull String repo, @Nullable Runnable callback) {
        new PluginFile(plugin).install(author, repo, callback);
    }

    /**
     * Uninstalls a plugin using Aliucord's standard uninstaller.
     *
     * @param plugin Plugin filename/name, without {@code .zip}.
     */
    public static void uninstallPlugin(@NonNull String plugin) {
        uninstallPlugin(plugin, null);
    }

    /**
     * Uninstalls a plugin using Aliucord's standard uninstaller.
     *
     * @param plugin Plugin filename/name, without {@code .zip}.
     * @param callback Optional callback posted on the main thread after uninstall finishes.
     */
    public static void uninstallPlugin(@NonNull String plugin, @Nullable Runnable callback) {
        new PluginFile(plugin).uninstall(callback);
    }

    /** Starts a loaded plugin. */
    public static void startPlugin(@NonNull String name) {
        PluginManager.startPlugin(name);
    }

    /** Stops a loaded plugin. */
    public static void stopPlugin(@NonNull String name) {
        PluginManager.stopPlugin(name);
    }

    /** Enables and starts a loaded plugin. */
    public static void enablePlugin(@NonNull String name) {
        PluginManager.enablePlugin(name);
    }

    /** Disables and stops a loaded plugin. */
    public static void disablePlugin(@NonNull String name) {
        PluginManager.disablePlugin(name);
    }

    /** Toggles a loaded plugin. */
    public static void togglePlugin(@NonNull String name) {
        PluginManager.togglePlugin(name);
    }

    /**
     * Checks whether a loaded plugin is enabled.
     *
     * @param name Plugin name.
     * @return Whether the plugin is enabled.
     */
    public static boolean isPluginEnabled(@NonNull String name) {
        return PluginManager.isPluginEnabled(name);
    }

    /**
     * Checks whether safe mode is enabled.
     *
     * @return Whether safe mode is enabled.
     */
    public static boolean isSafeModeEnabled() {
        return PluginManager.isSafeModeEnabled();
    }

    @NonNull
    private static String normalizeFilename(@NonNull String filename) {
        return filename.endsWith(".zip")
            ? filename.substring(0, filename.length() - ".zip".length())
            : filename;
    }
}
