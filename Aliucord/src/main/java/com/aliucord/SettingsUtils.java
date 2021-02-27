package com.aliucord;

import com.discord.stores.StoreStream;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SettingsUtils {
    public static boolean getBool(String key, boolean d) {
        return StoreStream.getUserSettings().prefs.getBoolean(key, d);
    }
    public static void setBool(String key, boolean v) {
        StoreStream.getUserSettings().prefs.edit().putBoolean(key, v).apply();
    }

    public static int getInt(String key, int d) {
        return StoreStream.getUserSettings().prefs.getInt(key, d);
    }
    public static void setInt(String key, int v) {
        StoreStream.getUserSettings().prefs.edit().putInt(key, v).apply();
    }

    public static String getString(String key, String d) {
        return StoreStream.getUserSettings().prefs.getString(key, d);
    }
    public static void setString(String key, String v) {
        StoreStream.getUserSettings().prefs.edit().putString(key, v).apply();
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
