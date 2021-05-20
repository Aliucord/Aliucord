/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api;

import com.aliucord.patcher.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class PatcherAPI {
    public static Runnable addPatch(String forClass, String fn, PatchFunction patch) {
        Patcher.addPatch(forClass, fn, patch);
        return () -> unpatch(forClass, fn, patch);
    }

    public static Runnable addPrePatch(String forClass, String fn, PrePatchFunction patch) {
        Patcher.addPrePatch(forClass, fn, patch);
        return () -> unpatchpre(forClass, fn, patch);
    }

    public static void unpatch(String clazz, String fn, PatchFunction patch) {
        Map<String, List<PatchFunction>> cp = Patcher.patches.get(clazz);
        if (cp == null) return;
        List<PatchFunction> p = cp.get(fn);
        if (p != null) p.remove(patch);
    }

    public static void unpatchpre(String clazz, String fn, PrePatchFunction patch) {
        Map<String, List<PrePatchFunction>> cp = Patcher.prePatches.get(clazz);
        if (cp == null) return;
        List<PrePatchFunction> p = cp.get(fn);
        if (p != null) p.remove(patch);
    }

    public List<Runnable> unpatches = new ArrayList<>();

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
