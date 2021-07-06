/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.patcher;

import rx.functions.Action1;
import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;

public class PinePrePatchFn extends MethodHook {
    private final Action1<Pine.CallFrame> p;

    /**
     * Calls {@link top.canyie.pine.Pine.CallFrame} patch block <strong>before</strong> the method has been invoked.
     * @param patch Patch block to execute.
     * @see top.canyie.pine.Pine.CallFrame
     */
    public PinePrePatchFn(Action1<Pine.CallFrame> patch) { p = patch; }

    @Override
    public void beforeCall(Pine.CallFrame callFrame) { p.call(callFrame); }
}
