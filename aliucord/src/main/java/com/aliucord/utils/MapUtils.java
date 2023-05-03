/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.Objects;

public final class MapUtils {
    /**
     * Finds the mapping key for Object val where Objects.equals(val, entry.value)
     * @param map The map to find the Object in
     * @param val The object to find the key of
     * @return Key of mapping or null if no such mapping exists
     */
    @Nullable
    public static <K, V> K getMapKey(@NonNull Map<K, V> map, @Nullable V val) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(val, entry.getValue())) return entry.getKey();
        }
        return null;
    }
}
