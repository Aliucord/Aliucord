/*
 * Copyright (c) 2021 Juby210
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

    public boolean getBool(String key, boolean d) {
        return SettingsUtils.getBool(keyPrefix + key, d);
    }
    public void setBool(String key, boolean v) {
        SettingsUtils.setBool(keyPrefix + key, v);
    }

    public int getInt(String key, int d) {
        return SettingsUtils.getInt(keyPrefix + key, d);
    }
    public void setInt(String key, int v) {
        SettingsUtils.setInt(keyPrefix + key, v);
    }

    public String getString(String key, String d) {
        return SettingsUtils.getString(keyPrefix + key, d);
    }
    public void setString(String key, String v) {
        SettingsUtils.setString(keyPrefix + key, v);
    }

    public <T> T getObject(String key, T d, Type type) {
        return SettingsUtils.getObject(keyPrefix + key, d, type);
    }
    public void setObject(String key, Object v) {
        SettingsUtils.setObject(keyPrefix + key, v);
    }
}
