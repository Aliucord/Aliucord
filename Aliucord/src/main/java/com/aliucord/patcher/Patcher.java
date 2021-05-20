/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.patcher;

import com.aliucord.Logger;
import com.aliucord.Main;
import com.aliucord.api.PatcherAPI;
import com.discord.app.AppActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

@SuppressWarnings({"unchecked", "unused"})
public class Patcher {
    public static Map<String, Map<String, List<PrePatchFunction>>> prePatches = new HashMap<>();
    public static Map<String, Map<String, List<PatchFunction>>> patches = new HashMap<>();
    public static Logger logger = new Logger("Patcher");

    static {
        String className = "com.discord.app.AppActivity";
        String fn = "onCreate";
        Patcher.addPrePatch(className, fn, new PrePatchFunction() {
            public PrePatchRes invoke(Object _this, List<Object> args) {
                Main.preInit((AppActivity) _this);
                PatcherAPI.unpatchpre(className, fn, this);
                return null;
            }
        });

        String fn2 = "c";
        Patcher.addPatch(className, fn2, new PatchFunction() {
            public Object invoke(Object _this, List<Object> args, Object ret) {
                Main.init((AppActivity) _this);
                PatcherAPI.unpatch(className, fn2, this);
                return ret;
            }
        });
    }

    public static PrePatchRes runPrePatches(Object _this, List<Object> args) {
        String[] names = getCallerNames();
        Map<String, List<PrePatchFunction>> cp = prePatches.get(names[0]);
        if (cp == null) return null;
        List<PrePatchFunction> p = cp.get(names[1]);
        if (p == null) return null;
        PrePatchRes res = null;
        Object ret = null;
        for (PrePatchFunction patch : p) {
            try {
                PrePatchRes res2 = patch.invoke(_this, args);
                if (res2 != null) res = res2;
            } catch (Throwable e) {
                logger.error("Failed to run prepatch on " + names[0] + "." + names[1], e);
            }
        }
        return res;
    }

    public static Object runPatches(Object _this, List<Object> args, Object ret) {
        String[] names = getCallerNames();
        Map<String, List<PatchFunction>> cp = patches.get(names[0]);
        if (cp == null) return ret;
        List<PatchFunction> p = cp.get(names[1]);
        if (p == null) return ret;
        try {
            for (PatchFunction patch : p) ret = patch.invoke(_this, args, ret);
        } catch (Throwable e) {
            logger.error("Failed to run patch on " + names[0] + "." + names[1], e);
        }
        return ret;
    }

    public static void addPrePatch(String forClass, String fn, PrePatchFunction patch) {
        Map<String, List<PrePatchFunction>> cp = prePatches.get(forClass);
        if (cp == null) {
            cp = new HashMap<>();
            prePatches.put(forClass, cp);
        }
        List<PrePatchFunction> p = cp.get(fn);
        if (p == null) {
            p = new ArrayList<>();
            cp.put(fn, p);
        }
        p.add(patch);
    }
    public static void addPatch(String forClass, String fn, PatchFunction patch) {
        Map<String, List<PatchFunction>> cp = patches.get(forClass);
        if (cp == null) {
            cp = new HashMap<>();
            patches.put(forClass, cp);
        }
        List<PatchFunction> p = cp.get(fn);
        if (p == null) {
            p = new ArrayList<>();
            cp.put(fn, p);
        }
        p.add(patch);
    }

    private static String[] getCallerNames() {
        StackTraceElement s = new Throwable().getStackTrace()[2];
        return new String[]{ s.getClassName(), s.getMethodName() };
    }
}
