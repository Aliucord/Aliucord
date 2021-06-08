/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import java.lang.reflect.Field;

public final class ReflectUtils {

    /**
     * Gets a field declared in the class.
     * @param instance Instance of the class where field is located.
     * @param fieldName Name of the field.
     * @param priv Whether the field is private or not.
     * @return Data stored in the field.
     * @throws NoSuchFieldException If the field doesn't exist.
     * @throws IllegalAccessException If the field is private and <code>priv</code> is set to false.
     */
    public static Object getField(Object instance, String fieldName, boolean priv) throws NoSuchFieldException, IllegalAccessException {
        return getField(instance.getClass(), instance, fieldName, priv);
    }

    /**
     * Gets a field declared in the class.
     * @param clazz {@link Class} where the field is stored
     * @param instance Instance of the <code>clazz</code>.
     * @param fieldName Name of the field.
     * @param priv Whether the field is private or not.
     * @return Data stored in the field.
     * @throws NoSuchFieldException If the field doesn't exist.
     * @throws IllegalAccessException If the field is private and <code>priv</code> is set to false.
     */
    public static Object getField(Class<?> clazz, Object instance, String fieldName, boolean priv) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        if (priv) field.setAccessible(true);
        return field.get(instance);
    }

    /**
     * Stores a value to the field declared in the class.
     * @param instance Instance of the class where field is located.
     * @param fieldName Name of the field.
     * @param v Value to store.
     * @param priv Whether the field is private or not.
     * @throws NoSuchFieldException If the field doesn't exist.
     * @throws IllegalAccessException If the field is private and <code>priv</code> is set to false.
     */
    public static void setField(Object instance, String fieldName, Object v, boolean priv) throws NoSuchFieldException, IllegalAccessException {
        setField(instance.getClass(), instance, fieldName, v, priv);
    }

    /**
     * Stores a value to the field declared in the class.
     * @param clazz {@link Class} where the field is stored
     * @param instance Instance of the <code>clazz</code>.
     * @param fieldName Name of the field.
     * @param v Value to store.
     * @param priv Whether the field is private or not.
     * @throws NoSuchFieldException If the field doesn't exist.
     * @throws IllegalAccessException If the field is private and <code>priv</code> is set to false.
     */
    public static void setField(Class<?> clazz, Object instance, String fieldName, Object v, boolean priv) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        if (priv) field.setAccessible(true);
        field.set(instance, v);
    }
}
