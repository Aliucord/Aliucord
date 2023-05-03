/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api;


import com.aliucord.SettingsUtilsJSON;

import java.lang.reflect.Type;
import java.util.List;

@SuppressWarnings("unused")
public class SettingsAPI {
    private SettingsUtilsJSON settings;
    private final String pluginName;

    /**
     * Creates a SettingsAPI for the specified plugin
     */
    public SettingsAPI(String plugin) {
        settings = new SettingsUtilsJSON(plugin);
        pluginName = plugin;
    }

    /**
     * Resets All Settings
     *
     * @return true if successful, else false
     */
    public boolean resetSettings() {
        var isSuccessful = settings.resetFile();
        settings = new SettingsUtilsJSON(pluginName);
        return isSuccessful;
    }

    /**
     * Removes Item from settings
     *
     * @param key Key of the value
     * @return True if removed, else false
     */
    public boolean remove(String key) {
        return settings.remove(key);
    }

    /**
     * Gets All Keys from settings
     * @return List of all keys
     */
    public List<String> getAllKeys(){
        return settings.getAllKeys();
    }

    /**
     * Toggles Boolean and returns it
     *
     * @param key Key of the value
     * @param defValue Default Value if setting doesn't exist
     * @return Toggled boolean
     */
    public boolean toggleBool(String key, boolean defValue) {
        return settings.toggleBool(key, defValue);
    }

    /**
     * Check if Key exists in settings
     *
     * @param key Key of the value
     * @return True if found, else false
     */
    public boolean exists(String key) {
        return settings.exists(key);
    }

    /**
     * Reads a {@link boolean} from the settings.
     * @param key Key of the setting.
     * @param defValue Default value of the setting.
     * @return Stored value, or default value if it doesn't exist.
     */
    public boolean getBool(String key, boolean defValue) {
        return settings.getBool(key, defValue);
    }

    /**
     * Writes a {@link boolean} to the settings.
     * @param key Key of the setting.
     * @param val Value of the setting.
     */
    public void setBool(String key, boolean val) {
        settings.setBool(key, val);
    }

    /**
     * Gets an {@link int} stored in the settings.
     * @param key Key of the setting.
     * @param defValue Default value of the setting.
     * @return Stored value, or default value if it doesn't exist.
     */
    public int getInt(String key, int defValue) {
        return settings.getInt(key, defValue);
    }

    /**
     * Writes an {@link int} to the settings.
     * @param key Key of the setting.
     * @param val Value of the setting.
     */
    public void setInt(String key, int val) {
        settings.setInt(key, val);
    }

    /**
     * Gets a {@link float} stored in the settings.
     * @param key Key of the setting.
     * @param defValue Default value of the setting.
     * @return Stored value, or default value if it doesn't exist.
     */
    public float getFloat(String key, float defValue) {
        return settings.getFloat(key, defValue);
    }

    /**
     * Writes a {@link float} to the settings.
     * @param key Key of the setting.
     * @param val Value of the setting.
     */
    public void setFloat(String key, float val) {
        settings.setFloat(key, val);
    }

    /**
     * Gets a {@link long} stored in the settings.
     * @param key Key of the setting.
     * @param defValue Default value of the setting.
     * @return Stored value, or default value if it doesn't exist.
     */
    public long getLong(String key, long defValue) {
        return settings.getLong(key, defValue);
    }

    /**
     * Writes a {@link long} to the settings.
     * @param key Key of the setting.
     * @param val Value of the setting.
     */
    public void setLong(String key, long val) {
        settings.setLong(key, val);
    }

    /**
     * Gets a {@link String} stored in the settings.
     * @param key Key of the setting.
     * @param defValue Default value of the setting.
     * @return Stored value, or default value if it doesn't exist.
     */
    public String getString(String key, String defValue) {
        return settings.getString(key, defValue);
    }

    /**
     * Writes a {@link String} to the settings.
     * @param key Key of the setting.
     * @param val Value of the setting.
     */
    public void setString(String key, String val) {
        settings.setString(key, val);
    }

    /**
     * Gets an {@link Object} stored in the settings.
     * @param key Key of the setting.
     * @param defValue Default value of the setting.
     * @return Stored value, or default value if it doesn't exist.
     */
    public <T> T getObject(String key, T defValue) {
        return settings.getObject(key, defValue);
    }

    /**
     * Gets an {@link Object} stored in the settings.
     * @param key Key of the setting.
     * @param defValue Default value of the setting.
     * @param type {@link Object} representing the data type.
     * @return Stored value, or default value if it doesn't exist.
     */
    public <T> T getObject(String key, T defValue, Type type) {
        return settings.getObject(key, defValue, type);
    }

    /**
     * Writes an {@link Object} to the settings.
     * @param key Key of the setting.
     * @param val Value of the setting.
     */
    public void setObject(String key, Object val) {
        settings.setObject(key, val);
    }

    /**
     * Get a value of an unknown type
     * @param key Key of the item
     */
    public Object getUnknown(String key, Object defValue) {
        if (defValue instanceof String) return getString(key, (String) defValue);
        if (defValue instanceof Boolean) return getBool(key, (Boolean) defValue);
        if (defValue instanceof Long) return getLong(key, (Long) defValue);
        if (defValue instanceof Float) return getFloat(key, (Float) defValue);
        if (defValue instanceof Integer) return getInt(key, (Integer) defValue);
        return getObject(key, defValue);
    }

    /**
     * Set a value of an unknown type
     * @param key Key of the item
     * @param value Value of the item
     */
    public void setUnknown(String key, Object value) {
        if (value instanceof String) setString(key, (String) value);
        else if (value instanceof Boolean) setBool(key, (Boolean) value);
        else if (value instanceof Long) setLong(key, (Long) value);
        else if (value instanceof Float) setFloat(key, (Float) value);
        else if (value instanceof Integer) setInt(key, (Integer) value);
        else setObject(key, value);
    }
}
