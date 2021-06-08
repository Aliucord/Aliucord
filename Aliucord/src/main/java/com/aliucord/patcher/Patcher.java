/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.patcher;

import android.os.Bundle;

import com.aliucord.Logger;
import com.aliucord.Main;
import com.discord.app.AppActivity;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;

import top.canyie.pine.Pine;
import top.canyie.pine.PineConfig;
import top.canyie.pine.callback.MethodHook;

@SuppressWarnings("unused")
public class Patcher {
    public static Logger logger = new Logger("Patcher");

    private static MethodHook.Unhook unhook1;
    private static MethodHook.Unhook unhook2;

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void init() {
        PineConfig.debug = false;
        PineConfig.disableHiddenApiPolicy = false;
        PineConfig.disableHiddenApiPolicyForPlatformDomain = false;
        Pine.setDebuggable(true);

        try {
            unhook1 = Pine.hook(AppActivity.class.getDeclaredMethod("onCreate", Bundle.class), new PinePrePatchFn(callFrame -> {
                Main.preInit((AppActivity) callFrame.thisObject);
                unhook1.unhook();
            }));

            unhook2 = Pine.hook(AppActivity.class.getDeclaredMethod("c", String.class, boolean.class), new PinePatchFn(callFrame -> {
                Main.init((AppActivity) callFrame.thisObject);
                unhook2.unhook();
            }));
        } catch (Throwable e) { logger.error(e); }
    }

    private static final ClassLoader cl = Objects.requireNonNull(Patcher.class.getClassLoader());

    public static Runnable addPatch(String forClass, String fn, Class<?>[] paramTypes, MethodHook hook) {
        try {
            return addPatch(cl.loadClass(forClass), fn, paramTypes, hook);
        } catch (Throwable e) { Patcher.logger.error(e); }
        return null;
    }

    public static Runnable addPatch(Class<?> clazz, String fn, Class<?>[] paramTypes, MethodHook hook) {
        try {
            return addPatch(clazz.getDeclaredMethod(fn, paramTypes), hook);
        } catch (Throwable e) { Patcher.logger.error(e); }
        return null;
    }

    public static Runnable addPatch(Member m, MethodHook hook) {
        return Pine.hook(m, hook)::unhook;
    }

    @SuppressWarnings("deprecation")
    public static MethodHook prePatchToMethodHook(PrePatchFunction patch) {
        return new MethodHook() {
            public void beforeCall(Pine.CallFrame callFrame) {
                try {
                    ArrayList<Object> args = new ArrayList<>(Arrays.asList(callFrame.args));
                    PrePatchRes res = patch.invoke(callFrame.thisObject, args);
                    if (res != null) callFrame.setResult(res.ret);
                    callFrame.args = args.toArray();
                } catch (Throwable e) { logger.error(e); }
            }
        };
    }

    @SuppressWarnings("deprecation")
    public static MethodHook patchToMethodHook(PatchFunction patch) {
        return new MethodHook() {
            public void afterCall(Pine.CallFrame callFrame) {
                try {
                    callFrame.setResult(patch.invoke(callFrame.thisObject, Arrays.asList(callFrame.args), callFrame.getResult()));
                } catch (Throwable e) { logger.error(e); }
            }
        };
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public static Runnable addPrePatch(String forClass, String fn, PrePatchFunction patch) {
        List<Runnable> unhooks = new ArrayList<>();
        try {
            Class<?> clazz = Objects.requireNonNull(Patcher.class.getClassLoader()).loadClass(forClass);
            MethodHook hook = prePatchToMethodHook(patch);
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(fn)) unhooks.add(Pine.hook(m, hook)::unhook);
            }
        } catch (Throwable e) { logger.error(e); }
        return () -> {
            for (Runnable unhook : unhooks) unhook.run();
        };
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public static Runnable addPatch(String forClass, String fn, PatchFunction patch) {
        List<Runnable> unhooks = new ArrayList<>();
        try {
            Class<?> clazz = Objects.requireNonNull(Patcher.class.getClassLoader()).loadClass(forClass);
            MethodHook hook = patchToMethodHook(patch);
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(fn)) unhooks.add(Pine.hook(m, hook)::unhook);
            }
        } catch (Throwable e) { logger.error(e); }
        return () -> {
            for (Runnable unhook : unhooks) unhook.run();
        };
    }
}
