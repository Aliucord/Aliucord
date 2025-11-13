/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliucord.Main;

import java.lang.reflect.*;

/** Utility class to ease Reflection */
@SuppressLint("DiscouragedPrivateApi")
@SuppressWarnings({ "unchecked", "unused" })
public final class ReflectUtils {
    private static Object unsafe;
    private static Method unsafeAllocIns;
    private static Field accessFlagsFields;

    /**
     * Creates new class instance without using a constructor
     *
     * @param clazz Class
     * @return Created instance
     */
    public static <T> T allocateInstance(@NonNull Class<T> clazz) {
        try {
            if (unsafeAllocIns == null) {
                var c = Class.forName("sun.misc.Unsafe");
                unsafe = ReflectUtils.getField(c, null, "theUnsafe");
                unsafeAllocIns = c.getMethod("allocateInstance", Class.class);
            }
            return (T) unsafeAllocIns.invoke(unsafe, clazz);
        } catch (Throwable e) {Main.logger.error(e);}
        return null;
    }

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
        Class<?>[] argTypes = null;
        if (args != null) {
            argTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i].getClass();
            }
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
     * @param clazz      The class
     * @param methodName The name of the method
     * @param args       The arguments to invoke the method with. arguments [ "hello", 12 ] would match someMethod(String s, int i)
     * @return The found method
     * @throws NoSuchMethodException No such constructor found
     */
    @NonNull
    public static Method getMethodByArgs(@NonNull Class<?> clazz, @NonNull String methodName, Object... args) throws NoSuchMethodException {
        Class<?>[] argTypes = null;
        if (args != null) {
            argTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i].getClass();
            }
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
     * @param clazz      The class holding the method
     * @param instance   The instance of the class to invoke the method on or null to invoke static method
     * @param methodName The name of the method
     * @param args       The arguments to invoke the method with. arguments [ "hello", 12 ] would match someMethod(String s, int i)
     * @return The result of invoking the method
     * @throws NoSuchMethodException     No such method found
     * @throws IllegalAccessException    This method is inaccessible
     * @throws InvocationTargetException An exception occurred while invoking this method
     */
    @Nullable
    public static Object invokeMethod(@NonNull Class<?> clazz, @Nullable Object instance, @NonNull String methodName, Object... args) throws ReflectiveOperationException {
        return getMethodByArgs(clazz, methodName, args).invoke(instance, args);
    }

    /**
     * Attempts to find and invoke the method matching the specified arguments
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Method} to improve performance drastically
     *
     * @param instance   The instance of the class to invoke the method on
     * @param methodName The name of the method
     * @param args       The arguments to invoke the method with. arguments [ "hello", 12 ] would match someMethod(String s, int i)
     * @return The result of invoking the method
     * @throws NoSuchMethodException     No such method found
     * @throws IllegalAccessException    This method is inaccessible
     * @throws InvocationTargetException An exception occurred while invoking this method
     */
    @Nullable
    public static Object invokeMethod(@NonNull Object instance, @NonNull String methodName, Object... args) throws ReflectiveOperationException {
        return invokeMethod(instance.getClass(), instance, methodName, args);
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
     * Override a field of a class.
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Field} to improve performance drastically
     *
     * @param instance  Instance of the class where the field is located.
     * @param fieldName Name of the field.
     * @param v         Value to store.
     * @throws NoSuchFieldException   If the field doesn't exist.
     * @throws IllegalAccessException If the field is inaccessible. Shouldn't happen.
     */
    public static void setField(@NonNull Object instance, @NonNull String fieldName, @Nullable Object v) throws NoSuchFieldException, IllegalAccessException {
        setField(instance.getClass(), instance, fieldName, v);
    }

    /**
     * Override a field of a class.
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Field} to improve performance drastically
     *
     * @param clazz     {@link Class} where the field is located.
     * @param instance  Instance of the <code>clazz</code> or null to set static field.
     * @param fieldName Name of the field.
     * @param v         Value to store.
     * @throws NoSuchFieldException   If the field doesn't exist.
     * @throws IllegalAccessException If the field is inaccessible. Shouldn't happen.
     */
    public static void setField(@NonNull Class<?> clazz, @Nullable Object instance, @NonNull String fieldName, @Nullable Object v) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, v);
    }

    /**
     * Override a final field of a class.
     * WARNING: If this field is of a primitive type, setting it may have no effect as the compiler will inline final primitives.
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Field} to improve performance drastically
     *
     * @param instance  Instance of the <code>clazz</code> or null to set static field.
     * @param fieldName Name of the field.
     * @param v         Value to store.
     * @throws NoSuchFieldException   If the field doesn't exist.
     * @throws IllegalAccessException If the field is inaccessible. Shouldn't happen.
     */
    public static void setFinalField(@Nullable Object instance, @NonNull String fieldName, @Nullable Object v) throws NoSuchFieldException, IllegalAccessException {
        setFinalField(instance.getClass(), instance, fieldName, v);
    }

    /**
     * Override a final field of a class.
     * WARNING: If this field is of a primitive type, setting it may have no effect as the compiler will inline final primitives.
     * Please note that this does not cache the lookup result, so if you need to call this many times
     * you should do it manually and cache the {@link Field} to improve performance drastically
     *
     * @param clazz     {@link Class} where the field is located.
     * @param instance  Instance of the <code>clazz</code> or null to set static field.
     * @param fieldName Name of the field.
     * @param v         Value to store.
     * @throws NoSuchFieldException   If the field doesn't exist.
     * @throws IllegalAccessException If the field is inaccessible. Shouldn't happen.
     */
    public static void setFinalField(@NonNull Class<?> clazz, @Nullable Object instance, @NonNull String fieldName, @Nullable Object v) throws NoSuchFieldException, IllegalAccessException {
        if (accessFlagsFields == null) {
            try {
                accessFlagsFields = Field.class.getDeclaredField("accessFlags");
            } catch (ReflectiveOperationException ignored) {
                try {
                    accessFlagsFields = Field.class.getDeclaredField("modifiers");
                } catch (ReflectiveOperationException ex) {
                    throw new RuntimeException("Failed to retrieve accessFlags/modifiers field", ex);
                }
            }
            accessFlagsFields.setAccessible(true);
        }

        var field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        accessFlagsFields.set(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(instance, v);
    }
}
