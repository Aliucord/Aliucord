/*
 * This file is part of Aliucord, an Android Discord client mod.
 *  Copyright (c) 2021 Juby210 & Vendicated
 *  Licensed under the Open Software License version 3.0
 */

package com.aliucord.patcher

import rx.functions.Action1
import top.canyie.pine.Pine.CallFrame
import top.canyie.pine.callback.MethodHook

/**
 * Calls [top.canyie.pine.Pine.CallFrame] patch block **instead of** this method.
 * @param patch Patch block to execute.
 * @see top.canyie.pine.Pine.CallFrame
 */
class PineInsteadFn(private val callback: Function1<CallFrame, Any?>) : MethodHook() {
    override fun beforeCall(callFrame: CallFrame) {
        try {
            callFrame.result = callback.invoke(callFrame)
        } catch (th: Throwable) {
            Patcher.logger.error(
                "Exception while hooking ${callFrame.method.declaringClass.name}.${callFrame.method.name}",
                th
            )
        }
    }
}
