/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.patcher

import rx.functions.Action1
import top.canyie.pine.Pine.CallFrame
import top.canyie.pine.callback.MethodHook

/**
 * Calls [top.canyie.pine.Pine.CallFrame] patch block **after** the method has been invoked.
 * @param patch Patch block to execute.
 * @see top.canyie.pine.Pine.CallFrame
 */
class PinePatchFn(private val callback: Action1<CallFrame>) : MethodHook() {
    override fun afterCall(callFrame: CallFrame) {
        try {
            callback.call(callFrame)
        } catch (th: Throwable) {
            Patcher.logger.error(
                "Exception while hooking ${callFrame.method.declaringClass.name}.${callFrame.method.name}",
                th
            )
        }
    }
}