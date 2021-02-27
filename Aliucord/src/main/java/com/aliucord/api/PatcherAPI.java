package com.aliucord.api;

import com.aliucord.patcher.PatchFunction;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PrePatchFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class PatcherAPI {
    public static Runnable addPatch(String forClass, String fn, PatchFunction<Object> patch) {
        Patcher.addPatch(forClass, fn, patch);
        return () -> unpatch(forClass, fn, patch);
    }

    public static Runnable addPrePatch(String forClass, String fn, PrePatchFunction patch) {
        Patcher.addPrePatch(forClass, fn, patch);
        return () -> unpatchpre(forClass, fn, patch);
    }

    public static void unpatch(String clazz, String fn, PatchFunction<Object> patch) {
        Map<String, ArrayList<PatchFunction<Object>>> cp = Patcher.patches.get(clazz);
        if (cp == null) return;
        ArrayList<PatchFunction<Object>> p = cp.get(fn);
        if (p != null) p.remove(patch);
    }

    public static void unpatchpre(String clazz, String fn, PrePatchFunction patch) {
        Map<String, ArrayList<PrePatchFunction>> cp = Patcher.prePatches.get(clazz);
        if (cp == null) return;
        ArrayList<PrePatchFunction> p = cp.get(fn);
        if (p != null) p.remove(patch);
    }

    public List<Runnable> unpatches = new ArrayList<>();

    public Runnable patch(String forClass, String fn, PatchFunction<Object> patch) {
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
//        for (Runnable unpatch = null; unpatches.size() < 0; unpatch = unpatches.get(0)) if (unpatch != null) unpatch.run();
//        Runnable unpatch;
//        while ((unpatch = unpatches.get(0)) != null) unpatch.run();
    }
}
