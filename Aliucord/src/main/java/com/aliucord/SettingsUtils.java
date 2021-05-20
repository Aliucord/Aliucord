/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SettingsUtils {
    private static final SharedPreferences prefs = Utils.getAppContext().getSharedPreferences("aliucord", Context.MODE_PRIVATE);

    public static boolean getBool(String key, boolean d) {
        return prefs.getBoolean(key, d);
    }
    public static void setBool(String key, boolean v) {
        prefs.edit().putBoolean(key, v).apply();
    }

    public static int getInt(String key, int d) {
        return prefs.getInt(key, d);
    }
    public static void setInt(String key, int v) {
        prefs.edit().putInt(key, v).apply();
    }

    public static String getString(String key, String d) {
        return prefs.getString(key, d);
    }
    public static void setString(String key, String v) {
        prefs.edit().putString(key, v).apply();
    }

    private static final Map<String, Object> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T getObject(String key, T d, Type type) {
        Object cached = cache.get(key);
        if (cached != null) return (T) cached;
        String json = getString(key, null);
        if (json == null) return d;
        T t = Utils.fromJson(json, type);
        return t == null ? d : t;
    }
    public static void setObject(String key, Object v) {
        cache.put(key, v);
        setString(key, Utils.toJson(v));
    }
}
