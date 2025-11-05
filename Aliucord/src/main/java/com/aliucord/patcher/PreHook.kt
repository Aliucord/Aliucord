/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.patcher

import de.robv.android.xposed.XC_MethodHook

/**
 * Invokes the provided [MethodHookCallback] before the hooked method or constructor runs.
 *
 * This small [XC_MethodHook] forwards the [beforeHookedMethod] event to the given
 * callback. Any exceptions thrown by the callback are caught and logged via
 * [Patcher.logger].
 *
 * @property callback the callback to run before the original method executes
 */
class PreHook(val callback: MethodHookCallback) : XC_MethodHook() {
    override fun beforeHookedMethod(param: MethodHookParam) {
        try {
            callback.call(param)
        } catch (th: Throwable) {
            Patcher.logger.error(
                "Exception while pre-hooking ${param.method.declaringClass.name}.${param.method.name}",
                th
            )
        }
    }
}
