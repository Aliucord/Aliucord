/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.patcher

import com.aliucord.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import top.canyie.pine.Pine
import top.canyie.pine.callback.MethodHook
import java.lang.reflect.Member
import java.util.*

object Patcher {
    var logger = Logger("Patcher")
    private val cl = Objects.requireNonNull(Patcher::class.java.classLoader)

    /**
     * Add a patch
     * @param forClass   The full name of the class to patch (e.g. com.aliucord.patcher.Patcher)
     * @param methodName The name of the method
     * @param paramTypes The types of the parameters (e.g. int.class, String.class)
     * @param hook       MethodHook
     * @return Unhook function
     */
    @JvmStatic
    @Deprecated("Use {@link #addPatch(String, String, Class[], XC_MethodHook)}")
    fun addPatch(
        forClass: String?,
        methodName: String?,
        paramTypes: Array<Class<*>?>,
        hook: MethodHook?
    ): Runnable? {
        try {
            return addPatch(cl.loadClass(forClass), methodName, paramTypes, hook)
        } catch (e: Throwable) {
            logger.error(e)
        }
        return null
    }

    /**
     * Add a patch
     * @param clazz      Class to patch
     * @param methodName The name of the method
     * @param paramTypes The types of the parameters (e.g. int.class, String.class)
     * @param hook       MethodHook
     * @return Unhook function
     */
    @JvmStatic
    @Deprecated("Use {@link #addPatch(Member, XC_MethodHook)}")
    fun addPatch(
        clazz: Class<*>,
        methodName: String?,
        paramTypes: Array<Class<*>?>,
        hook: MethodHook?
    ): Runnable? {
        try {
            return addPatch(clazz.getDeclaredMethod(methodName, *paramTypes), hook)
        } catch (e: Throwable) {
            logger.error(e)
        }
        return null
    }

    /**
     * Add a patch
     * @param member The member (method, constructor) to patch
     * @param hook   MethodHook
     * @return Unhook function
     */
    @JvmStatic
    @Deprecated("Use {@link #addPatch(Class, String, Class[], XC_MethodHook)}")
    fun addPatch(member: Member?, hook: MethodHook?): Runnable {
        return Runnable { Pine.hook(member, hook).unhook() }
    }

    /**
     * Add a patch
     * @param member The member (method, constructor) to patch
     * @param hook   MethodHook
     * @return Unhook
     */
    @JvmStatic
    fun addPatch(member: Member?, hook: XC_MethodHook?): XC_MethodHook.Unhook {
        return XposedBridge.hookMethod(member, hook)
    }

    /**
     * Add a patch
     * @param clazz      Class to patch
     * @param methodName The name of the method
     * @param paramTypes The types of the parameters (e.g. int.class, String.class)
     * @param hook       MethodHook
     * @return Unhook
     */
    @JvmStatic
    fun addPatch(
        clazz: Class<*>,
        methodName: String,
        paramTypes: Array<Class<*>>,
        hook: XC_MethodHook
    ): XC_MethodHook.Unhook? {
        return try {
            addPatch(clazz.getDeclaredMethod(methodName, *paramTypes), hook)
        } catch (e: Throwable) {
            logger.error(e)
            null
        }
    }

    /**
     * Add a patch
     * @param forClass   The full name of the class to patch (e.g. com.aliucord.patcher.Patcher)
     * @param methodName The name of the method
     * @param paramTypes The types of the parameters (e.g. int.class, String.class)
     * @param hook       MethodHook
     * @return Unhook
     */
    @JvmStatic
    fun addPatch(
        forClass: String,
        methodName: String,
        paramTypes: Array<Class<*>>,
        hook: XC_MethodHook
    ): XC_MethodHook.Unhook? {
        return try {
            addPatch(cl.loadClass(forClass), methodName, paramTypes, hook)
        } catch (e: Throwable) {
            logger.error(e)
            null
        }
    }
}
