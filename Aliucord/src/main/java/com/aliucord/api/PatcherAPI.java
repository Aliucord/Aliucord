/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api;

import com.aliucord.patcher.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import top.canyie.pine.callback.MethodHook;

@SuppressWarnings({"unused", "deprecation"})
public class PatcherAPI {
    @Deprecated
    public static Runnable addPatch(String forClass, String fn, PatchFunction patch) {
        return Patcher.addPatch(forClass, fn, patch);
    }

    @Deprecated
    public static Runnable addPrePatch(String forClass, String fn, PrePatchFunction patch) {
        return Patcher.addPrePatch(forClass, fn, patch);
    }

    public List<Runnable> unpatches = new ArrayList<>();

    private Runnable createUnpatch(Runnable _unpatch) {
        Runnable unpatch = new Runnable() {
            public void run() {
                _unpatch.run();
                unpatches.remove(this);
            }
        };
        unpatches.add(unpatch);
        return unpatch;
    }

    public Runnable patch(String forClass, String fn, Class<?>[] paramTypes, MethodHook hook) {
        return createUnpatch(Patcher.addPatch(forClass, fn, paramTypes, hook));
    }

    public Runnable patch(Class<?> clazz, String fn, Class<?>[] paramTypes, MethodHook hook) {
        return createUnpatch(Patcher.addPatch(clazz, fn, paramTypes, hook));
    }

    public Runnable patch(Method m, MethodHook hook) {
        return createUnpatch(Patcher.addPatch(m, hook));
    }

    @Deprecated
    public Runnable patch(String forClass, String fn, PatchFunction patch) {
        Runnable unpatch = PatcherAPI.addPatch(forClass, fn, patch);
        Runnable _unpatch = new Runnable() {
            public void run() {
                unpatch.run();
                unpatches.remove(this);
            }
        };
        unpatches.add(_unpatch);
        return _unpatch;
    }

    @Deprecated
    public Runnable prePatch(String forClass, String fn, PrePatchFunction patch) {
        Runnable unpatch = PatcherAPI.addPrePatch(forClass, fn, patch);
        Runnable _unpatch = new Runnable() {
            public void run() {
                unpatch.run();
                unpatches.remove(this);
            }
        };
        unpatches.add(_unpatch);
        return _unpatch;
    }

    public void unpatchAll() {
        Object[] runnables = unpatches.toArray();
        for (Object unpatch : runnables) ((Runnable) unpatch).run();
    }
}
