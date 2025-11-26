/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.patcher

import com.aliucord.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.Unhook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Member

/**
 * Helper for applying Xposed method hooks.
 */
object Patcher {
    internal val logger = Logger("Patcher")

    private val cl = Patcher::class.java.classLoader!!

    /**
     * Hook the given reflective member (method or constructor) with the provided hook.
     *
     * @param member the method or constructor to hook
     * @param hook the XC_MethodHook to apply
     * @return an [Unhook] that can remove the applied hook
     */
    @JvmStatic
    fun addPatch(member: Member, hook: XC_MethodHook): Unhook {
        return XposedBridge.hookMethod(member, hook)
    }

    /**
     * Hook the method with the given name on the specified class.
     *
     * @param clazz the class containing the method
     * @param methodName the name of the method to hook
     * @param paramTypes the parameter types (defaults to none)
     * @param hook the [XC_MethodHook] to apply
     * @return an [Unhook] that can remove the applied hook, or null on failure
     */
    @JvmStatic
    @JvmOverloads
    fun addPatch(
        clazz: Class<*>,
        methodName: String,
        paramTypes: Array<Class<*>> = emptyArray(),
        hook: XC_MethodHook
    ): Unhook? {
        return try {
            addPatch(clazz.getDeclaredMethod(methodName, *paramTypes), hook)
        } catch (e: Throwable) {
            logger.error(e)
            null
        }
    }

    /**
     * Load the class by name and hook the specified method.
     *
     * @param forClass the full name of the class to load (e.g. com.example.MyClass)
     * @param methodName the name of the method to hook
     * @param paramTypes the parameter types (defaults to none)
     * @param hook the [XC_MethodHook] to apply
     * @return an [Unhook] that can remove the applied hook, or null on failure
     */
    @JvmStatic
    @JvmOverloads
    fun addPatch(
        forClass: String,
        methodName: String,
        paramTypes: Array<Class<*>> = emptyArray(),
        hook: XC_MethodHook
    ): Unhook? {
        return try {
            addPatch(cl.loadClass(forClass), methodName, paramTypes, hook)
        } catch (e: Throwable) {
            logger.error(e)
            null
        }
    }
}
