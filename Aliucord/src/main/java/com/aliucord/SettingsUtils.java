/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Utility class to store and retrieve preferences
 * This is only used for Aliucord internally
 */
@Deprecated
public class SettingsUtils {
    private static final SharedPreferences prefs = Utils.getAppContext().getSharedPreferences("aliucord", Context.MODE_PRIVATE);

    /**
     * Removes Item from settings
     *
     * @param key Key of the value
     */
    @Deprecated
    public static void remove(String key) {
        prefs.edit().remove(key).apply();
    }

    /**
     * Gets all settings
     *
     * @return All settings
     */
    @Deprecated
    public static Map<String, ?> getAll() {
        return prefs.getAll();
    }
}
