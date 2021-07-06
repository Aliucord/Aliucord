/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public final class ReflectUtils {
    /**
     * Gets the constructor for class T matching the specified arguments
     * @param clazz T.class
     * @param args The arguments that should be passed to the constructor. arguments [ "hello", 12 ] would match constructor(String s, int i)
     * @param <T> The class
     * @return The found constructor
     * @throws NoSuchMethodException No such constructor found
     */
    public static <T> Constructor<T> getConstructorByArgs(Class<T> clazz, Object... args) throws NoSuchMethodException {
        if (args.length == 0) return clazz.getDeclaredConstructor();

        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        return clazz.getDeclaredConstructor(argTypes);
    }

    /**
     * Attempts to find and invoke the constructor of class T matching the specified arguments
     * @param clazz T.class
     * @param args The arguments to invoke the constructor with. arguments [ "hello", 12 ] would match constructor(String s, int i)
     * @param <T> The class
     * @return The constructed Object
     * @throws NoSuchMethodException No such constructor found
     * @throws IllegalAccessException This constructor is private
     * @throws InvocationTargetException An exception occurred while invoking this constructor
     * @throws InstantiationException This class cannot be constructed (is abstract, interface, etc)
     */
    public static <T> T invokeConstructorWithArgs(Class<T> clazz, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return getConstructorByArgs(clazz, args).newInstance(args);
    }

    /**
     * Gets a field declared in the class.
     * @param instance Instance of the class where the field is located.
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
     * @param clazz {@link Class} where the field is located.
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
     * @param instance Instance of the class where the field is located.
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
     * @param clazz {@link Class} where the field is located.
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
