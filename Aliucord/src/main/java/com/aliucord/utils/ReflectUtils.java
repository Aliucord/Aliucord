/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import java.lang.reflect.Field;

public final class ReflectUtils {
    public static Object getField(Object instance, String fieldName, boolean priv) throws NoSuchFieldException, IllegalAccessException {
        return getField(instance.getClass(), instance, fieldName, priv);
    }
    public static Object getField(Class<?> clazz, Object instance, String fieldName, boolean priv) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        if (priv) field.setAccessible(true);
        return field.get(instance);
    }

    public static void setField(Object instance, String fieldName, Object v, boolean priv) throws NoSuchFieldException, IllegalAccessException {
        setField(instance.getClass(), instance, fieldName, v, priv);
    }
    public static void setField(Class<?> clazz, Object instance, String fieldName, Object v, boolean priv) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        if (priv) field.setAccessible(true);
        field.set(instance, v);
    }
}
