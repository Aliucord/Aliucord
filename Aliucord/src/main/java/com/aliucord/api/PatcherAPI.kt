/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.api

import com.aliucord.Logger
import com.aliucord.patcher.Hook
import com.aliucord.patcher.MethodHookCallback
import com.aliucord.patcher.Patcher.addPatch
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.Unhook
import java.lang.reflect.Member

typealias Unpatch = Runnable

/**
 * Runtime patching helper that wraps Xposed hooks and tracks created unpatch actions.
 *
 * Use instances to apply hooks via the `patch` methods. Each patch returns an [Unpatch]
 * callback that will remove the applied hook when invoked. Call [unpatchAll] to
 * run all registered unpatch actions and clear the internal registry.
 *
 * @param logger logger used for error reporting
 */
class PatcherAPI internal constructor(
    @PublishedApi
    internal val logger: Logger
) {
    private val unpatches: MutableList<Unpatch> = mutableListOf()

    private fun createUnpatch(unhook: Unhook?): Unpatch {
        val unpatch = Unpatch { unhook?.unhook() }
        unpatches += unpatch
        return unpatch
    }

    /**
     * Patch a method by class name.
     *
     * @param forClass full name of the class containing the method
     * @param fn method name to patch
     * @param paramTypes parameter types to select an overload
     * @param hook the [XC_MethodHook] callback to apply
     * @return an [Unpatch] that will remove the applied hook
     */
    @JvmOverloads
    fun patch(
        forClass: String,
        fn: String,
        paramTypes: Array<Class<*>> = emptyArray(),
        hook: XC_MethodHook
    ): Unpatch {
        return createUnpatch(addPatch(forClass, fn, paramTypes, hook))
    }

    /**
     * Patch a method by [Class].
     *
     * @param clazz the class containing the method
     * @param fn method name to patch
     * @param paramTypes parameter types to select an overload
     * @param hook the [XC_MethodHook] callback to apply
     * @return an [Unpatch] that will remove the applied hook
     */
    @JvmOverloads
    fun patch(
        clazz: Class<*>,
        fn: String,
        paramTypes: Array<Class<*>> = emptyArray(),
        hook: XC_MethodHook
    ): Unpatch {
        return createUnpatch(addPatch(clazz, fn, paramTypes, hook))
    }

    /**
     * Patch a specific method or constructor.
     *
     * @param m the reflective [Member] (method or constructor) to hook
     * @param hook the [XC_MethodHook] callback to apply
     * @return an [Unpatch] that will remove the applied hook
     */
    fun patch(m: Member, hook: XC_MethodHook): Unpatch {
        return createUnpatch(addPatch(m, hook))
    }

    /**
     * Patch a specific method or constructor using a [MethodHookCallback].
     *
     * @param m the reflective [Member] (method or constructor) to hook
     * @param callback the callback wrapper to apply
     * @return an [Unpatch] that will remove the applied hook
     */
    fun patch(m: Member, callback: MethodHookCallback): Unpatch {
        return createUnpatch(addPatch(m, Hook(callback)))
    }

    /**
     * Invoke all registered unpatch actions and clear the registry.
     *
     * This will remove all applied hooks that were created through this [PatcherAPI] instance.
     */
    fun unpatchAll() {
        unpatches.forEach(Unpatch::run)
        unpatches.clear()
    }
}
