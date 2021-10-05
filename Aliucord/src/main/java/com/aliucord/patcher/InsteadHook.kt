/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.patcher

import de.robv.android.xposed.XC_MethodHook
import java.lang.reflect.Member

/**
 * Runs the specified [callback] **instead of** the hooked [Member]
 *
 * @property callback The callback to run instead of the method
 */
class InsteadHook(val callback: Function1<MethodHookParam, Any?>) : XC_MethodHook() {
    override fun beforeHookedMethod(param: MethodHookParam) {
        try {
            param.result = callback.invoke(param);
        } catch (th: Throwable) {
            Patcher.logger.error(
                "Exception while replacing ${param.method.declaringClass.name}.${param.method.name}",
                th
            )
        }
    }

    companion object {
        /**
         * [InsteadHook] that always returns null
         */
        @JvmField
        val DO_NOTHING = returnConstant(null)

        /**
         * [InsteadHook] that always returns the specified [constant]
         *
         * @param constant Constant to return
         */
        @JvmStatic
        fun returnConstant(constant: Any?) = InsteadHook { constant }
    }
}