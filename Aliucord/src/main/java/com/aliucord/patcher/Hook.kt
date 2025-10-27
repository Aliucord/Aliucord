/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.patcher

import de.robv.android.xposed.XC_MethodHook
import rx.functions.Action1
import java.lang.reflect.Member

/**
 * A callback invoked with the Xposed method hook parameter when a hooked method runs.
 */
typealias MethodHookCallback = Action1<XC_MethodHook.MethodHookParam>

/**
 * Runs the provided [callback] after the hooked [Member] executes.
 *
 * This is a small XC_MethodHook implementation that forwards the [afterHookedMethod]
 * event to a higher-level [MethodHookCallback]. Exceptions thrown by the callback
 * are caught and logged via [Patcher.logger].
 *
 * @property callback the callback to execute after the original method completes
 */
class Hook(val callback: MethodHookCallback) : XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam) {
        try {
            callback.call(param)
        } catch (th: Throwable) {
            Patcher.logger.error(
                "Exception while hooking ${param.method.declaringClass.name}.${param.method.name}",
                th
            )
        }
    }
}
