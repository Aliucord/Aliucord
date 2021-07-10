/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api;

import com.aliucord.SettingsUtils;

import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class SettingsAPI {
    public final String keyPrefix;

    public SettingsAPI(String plugin) {
        keyPrefix = "AC_" + plugin + "_";
    }

    /**
     * Reads a {@link boolean} from the settings.
     * @param key Key of the setting.
     * @param d Default value of the setting.
     * @return Stored value, or default value if it doesn't exist.
     */
    public boolean getBool(String key, boolean d) {
        return SettingsUtils.getBool(keyPrefix + key, d);
    }

    /**
     * Writes a {@link boolean} to the settings.
     * @param key Key of the setting.
     * @param v Value of the setting.
     */
    public void setBool(String key, boolean v) {
        SettingsUtils.setBool(keyPrefix + key, v);
    }

    /**
     * Gets an {@link int} stored in the settings.
     * @param key Key of the setting.
     * @param d Default value of the setting.
     * @return Stored value, or default value if it doesn't exist.
     */
    public int getInt(String key, int d) {
        return SettingsUtils.getInt(keyPrefix + key, d);
    }

    /**
     * Writes an {@link int} to the settings.
     * @param key Key of the setting.
     * @param v Value of the setting.
     */
    public void setInt(String key, int v) {
        SettingsUtils.setInt(keyPrefix + key, v);
    }

    /**
     * Gets a {@link String} stored in the settings.
     * @param key Key of the setting.
     * @param d Default value of the setting.
     * @return Stored value, or default value if it doesn't exist.
     */
    public String getString(String key, String d) {
        return SettingsUtils.getString(keyPrefix + key, d);
    }

    /**
     * Writes a {@link String} to the settings.
     * @param key Key of the setting.
     * @param v Value of the setting.
     */
    public void setString(String key, String v) {
        SettingsUtils.setString(keyPrefix + key, v);
    }

    /**
     * Gets an {@link Object} stored in the settings.
     * @param key Key of the setting.
     * @param d Default value of the setting.
     * @param type {@link Object} representing the data type.
     * @return Stored value, or default value if it doesn't exist.
     */
    public <T> T getObject(String key, T d, Type type) {
        return SettingsUtils.getObject(keyPrefix + key, d, type);
    }

    /**
     * Writes an {@link Object} to the settings.
     * @param key Key of the setting.
     * @param v Value of the setting.
     */
    public void setObject(String key, Object v) {
        SettingsUtils.setObject(keyPrefix + key, v);
    }
}
