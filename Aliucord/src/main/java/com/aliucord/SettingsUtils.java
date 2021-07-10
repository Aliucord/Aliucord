/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/** Utility class to store and retrieve preferences */
@SuppressWarnings("unused")
public class SettingsUtils {
    private static final SharedPreferences prefs = Utils.getAppContext().getSharedPreferences("aliucord", Context.MODE_PRIVATE);

    /**
     * Get a boolean value from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    public static boolean getBool(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    /**
     * Set a boolean item
     * @param key Key of the item
     * @param val Value
     */
    public static void setBool(String key, boolean val) {
        prefs.edit().putBoolean(key, val).apply();
    }

    /**
     * Get an int value from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    public static int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    /**
     * Set an int item
     * @param key Key of the item
     * @param val Value
     */
    public static void setInt(String key, int val) {
        prefs.edit().putInt(key, val).apply();
    }

    /**
     * Get a String value from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    public static String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    /**
     * Set a String item
     * @param key Key of the item
     * @param val Value
     */
    public static void setString(String key, String val) {
        prefs.edit().putString(key, val).apply();
    }

    private static final Map<String, Object> cache = new HashMap<>();

    /**
     * Get an Object value from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    public static <T> T getObject(String key, T defValue) {
        return getObject(key, defValue, defValue.getClass());
    }

    /**
     * Get an Object value from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @param type Type of the object
     * @return Value if found, else the defValue
     */
    @SuppressWarnings("unchecked")
    public static <T> T getObject(String key, T defValue, Type type) {
        Object cached = cache.get(key);
        if (cached != null)
            try { return (T) cached; } catch (Throwable ignored) {};
        String json = getString(key, null);
        if (json == null) return defValue;
        T t = Utils.fromJson(json, type);
        return t == null ? defValue : t;
    }

    /**
     * Set an Object item
     * @param key Key of the item
     * @param val Value
     */
    public static void setObject(String key, Object val) {
        cache.put(key, val);
        setString(key, Utils.toJson(val));
    }
}
