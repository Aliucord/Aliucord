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
 * Runs the specified [callback] **after** the hooked [Member]
 *
 * @property callback The callback to run after this method
 */
class Hook(val callback: Action1<MethodHookParam>) : XC_MethodHook() {
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