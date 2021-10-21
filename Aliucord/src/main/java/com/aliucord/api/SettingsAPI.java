/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api;


import com.aliucord.SettingsUtilsJSON;

import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class SettingsAPI {
    /** Prefix for all settings keys: AC_{PLUGIN_NAME}_ */
    public final String keyPrefix;
    SettingsUtilsJSON settings;
    /** Creates a SettingsAPI for the specified plugin */
    public SettingsAPI(String plugin) {
        keyPrefix = "AC_" + plugin + "_";
        settings= new SettingsUtilsJSON(plugin);
    }

    /**
     * Resets All Settings
     * @return true if successful, else false
     */
    public boolean resetSettings(){
       return settings.resetSettings();
    }


    /**
     * Removes Item from settings
     * @param key Key of the value
     * @return True if removed, else false
     */
    public boolean remove(String key){
        return settings.remove(key);
    }

    /**
     * Toggles Boolean and returns it
     * @param key Key of the value
     * @param defValue Default Value if setting doesn't exist
     * @return Toggled boolean
     */
    public boolean toggleBool(String key, boolean defValue){
        return settings.toggleBool(key,defValue);
    }
    /**
     * Check if Key exists in settings
     * @param key Key of the value
     * @return True if found, else false
     */
    public boolean exists(String key){
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
}
