/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.*;

/** Utility class to ease Reflection */
public final class ReflectUtils {
    /**
     * Gets the constructor for class T matching the specified arguments
     *
     * @param clazz T.class
     * @param args  The arguments that should be passed to the constructor. arguments [ "hello", 12 ] would match constructor(String s, int i)
     * @param <T>   The class
     * @return The found constructor
     * @throws NoSuchMethodException No such constructor found
     */
    public static <T> Constructor<T> getConstructorByArgs(@NonNull Class<T> clazz, Object... args) throws NoSuchMethodException {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }

        Constructor<T> c = clazz.getDeclaredConstructor(argTypes);
        c.setAccessible(true);
        return c;
    }

    /**
     * Attempts to find and invoke the constructor of class T matching the specified arguments
     *
     * @param clazz T.class
     * @param args  The arguments to invoke the constructor with. arguments [ "hello", 12 ] would match constructor(String s, int i)
     * @param <T>   The class
     * @return The constructed Object
     * @throws NoSuchMethodException     No such constructor found
     * @throws IllegalAccessException    This constructor is inaccessible
     * @throws InvocationTargetException An exception occurred while invoking this constructor
     * @throws InstantiationException    This class cannot be constructed (is abstract, interface, etc)
     */
    public static <T> T invokeConstructorWithArgs(@NonNull Class<T> clazz, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return getConstructorByArgs(clazz, args).newInstance(args);
    }

    /**
     * Attempts to find and invoke the method matching the specified arguments
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Method} to improve performance drastically
     *
     * @param clazz The class
     * @param methodName The name of the method
     * @param args  The arguments to invoke the method with. arguments [ "hello", 12 ] would match someMethod(String s, int i)
     * @return The found method
     * @throws NoSuchMethodException No such constructor found
     */
    @NonNull
    public static Method getMethodByArgs(@NonNull Class<?> clazz, @NonNull String methodName, Object... args) throws NoSuchMethodException {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }

        Method m = clazz.getDeclaredMethod(methodName, argTypes);
        m.setAccessible(true);
        return m;
    }

    /**
     * Attempts to find and invoke the method matching the specified arguments
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Method} to improve performance drastically
     *
     * @param clazz The class holding the method
     * @param instance The instance of the class to invoke the method on or null to invoke static method
     * @param methodName The name of the method
     * @param args  The arguments to invoke the method with. arguments [ "hello", 12 ] would match someMethod(String s, int i)
     * @return The result of invoking the method
     * @throws NoSuchMethodException     No such method found
     * @throws IllegalAccessException    This method is inaccessible
     * @throws InvocationTargetException An exception occurred while invoking this method
     */
    @Nullable
    public static Object invokeMethodWithArgs(@NonNull Class<?> clazz, @Nullable Object instance, @NonNull String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return getMethodByArgs(clazz, methodName, args).invoke(instance, args);
    }

    /**
     * Attempts to find and invoke the method matching the specified arguments
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Method} to improve performance drastically
     *
     * @param instance The instance of the class to invoke the method on
     * @param methodName The name of the method
     * @param args  The arguments to invoke the method with. arguments [ "hello", 12 ] would match someMethod(String s, int i)
     * @return The result of invoking the method
     * @throws NoSuchMethodException     No such method found
     * @throws IllegalAccessException    This method is inaccessible
     * @throws InvocationTargetException An exception occurred while invoking this method
     */
    @Nullable
    public static Object invokeMethodWithArgs(@NonNull Object instance, @NonNull String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return getMethodByArgs(instance.getClass(), methodName, args).invoke(instance, args);
    }

    /**
     * Gets a field declared in the class.
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Field} to improve performance drastically
     *
     * @param instance  Instance of the class where the field is located.
     * @param fieldName Name of the field.
     * @return Data stored in the field.
     * @throws NoSuchFieldException   If the field doesn't exist.
     * @throws IllegalAccessException If the field is inaccessible
     */
    @Nullable
    public static Object getField(@NonNull Object instance, @NonNull String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return getField(instance.getClass(), instance, fieldName);
    }

    /**
     * @deprecated Use {@link #getField(Object, String)}
     */
    @Nullable
    @Deprecated
    public static Object getField(@NonNull Object instance, @NonNull String fieldName, boolean _priv) throws NoSuchFieldException, IllegalAccessException {
        return getField(instance.getClass(), instance, fieldName);
    }

    /**
     * Gets a field declared in the class.
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Field} to improve performance drastically
     *
     * @param clazz     {@link Class} where the field is located.
     * @param instance  Instance of the <code>clazz</code> or null to get static field
     * @param fieldName Name of the field.
     * @return Data stored in the field.
     * @throws NoSuchFieldException   If the field doesn't exist.
     * @throws IllegalAccessException If the field is inaccessible.
     */
    @Nullable
    public static Object getField(@NonNull Class<?> clazz, @Nullable Object instance, @NonNull String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }

    /**
     * @deprecated Use {@link #getField(Class, Object, String)}
     */
    @Nullable
    @Deprecated
    public static Object getField(@NonNull Class<?> clazz, @Nullable Object instance, @NonNull String fieldName, boolean _priv) throws NoSuchFieldException, IllegalAccessException {
        return getField(clazz, instance, fieldName);
    }

    /**
     * Stores a value to the field declared in the class.
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Field} to improve performance drastically
     *
     * @param instance  Instance of the class where the field is located.
     * @param fieldName Name of the field.
     * @param v         Value to store.
     * @throws NoSuchFieldException   If the field doesn't exist.
     * @throws IllegalAccessException If the field is inaccessible.
     */
    public static void setField(@NonNull Object instance, @NonNull String fieldName, @Nullable Object v) throws NoSuchFieldException, IllegalAccessException {
        setField(instance.getClass(), instance, fieldName, v);
    }

    /**
     * @deprecated Use {@link #setField(Object, String, Object)}
     */
    @Deprecated
    public static void setField(@NonNull Object instance, @NonNull String fieldName, @Nullable Object v, boolean _priv) throws NoSuchFieldException, IllegalAccessException {
        setField(instance.getClass(), instance, fieldName, v);
    }

    /**
     * Stores a value to the field declared in the class.
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Field} to improve performance drastically
     *
     * @param clazz     {@link Class} where the field is located.
     * @param instance  Instance of the <code>clazz</code> or null to set static field.
     * @param fieldName Name of the field.
     * @param v         Value to store.
     * @throws NoSuchFieldException   If the field doesn't exist.
     * @throws IllegalAccessException If the field is inaccessible.
     */
    public static void setField(@NonNull Class<?> clazz, @Nullable Object instance, @NonNull String fieldName, @Nullable Object v) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, v);
    }

    /**
     * @deprecated Use {@link #setField(Class, Object, String, Object)}
     */
    @Deprecated
    public static void setField(@NonNull Class<?> clazz, @Nullable Object instance, @NonNull String fieldName, @Nullable Object v, boolean _priv) throws NoSuchFieldException, IllegalAccessException {
        setField(clazz, instance, fieldName, v);
    }
}
