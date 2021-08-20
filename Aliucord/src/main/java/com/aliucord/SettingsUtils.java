/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.Context;
import android.content.SharedPreferences;

import com.aliucord.utils.GsonUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/** Utility class to store and retrieve preferences */
@SuppressWarnings("unused")
public class SettingsUtils {
    private static final SharedPreferences prefs = Utils.getAppContext().getSharedPreferences("aliucord", Context.MODE_PRIVATE);

    /**
     * Get a {@link boolean} from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    public static boolean getBool(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    /**
     * Set a {@link boolean} item
     * @param key Key of the item
     * @param val Value
     */
    public static void setBool(String key, boolean val) {
        prefs.edit().putBoolean(key, val).apply();
    }

    /**
     * Get an {@link int} from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    public static int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    /**
     * Set an {@link int} item
     * @param key Key of the item
     * @param val Value
     */
    public static void setInt(String key, int val) {
        prefs.edit().putInt(key, val).apply();
    }

    /**
     * Get a {@link float} from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    public static float getFloat(String key, float defValue) {
        return prefs.getFloat(key, defValue);
    }

    /**
     * Set a {@link float} item
     * @param key Key of the item
     * @param val Value
     */
    public static void setFloat(String key, float val) {
        prefs.edit().putFloat(key, val).apply();
    }

    /**
     * Get a {@link long} from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    public static long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    /**
     * Set a {@link long} item
     * @param key Key of the item
     * @param val Value
     */
    public static void setLong(String key, long val) {
        prefs.edit().putLong(key, val).apply();
    }

    /**
     * Get a {@link String} from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    public static String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    /**
     * Set a {@link String} item
     * @param key Key of the item
     * @param val Value
     */
    public static void setString(String key, String val) {
        prefs.edit().putString(key, val).apply();
    }

    private static final Map<String, Object> cache = new HashMap<>();

    /**
     * Get an {@link Object} from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    public static <T> T getObject(String key, T defValue) {
        return getObject(key, defValue, defValue.getClass());
    }

    /**
     * Get an {@link Object} from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @param type Type of the object
     * @return Value if found, else the defValue
     */
    @SuppressWarnings("unchecked")
    public static <T> T getObject(String key, T defValue, Type type) {
        Object cached = cache.get(key);
        if (cached != null)
            try { return (T) cached; } catch (Throwable ignored) {}
        String json = getString(key, null);
        if (json == null) return defValue;
        T t = GsonUtils.fromJson(json, type);
        return t == null ? defValue : t;
    }

    /**
     * Set an {@link Object} item
     * @param key Key of the item
     * @param val Value
     */
    public static void setObject(String key, Object val) {
        cache.put(key, val);
        setString(key, GsonUtils.toJson(val));
    }
}
