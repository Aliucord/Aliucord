/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api;

import androidx.annotation.NonNull;

import com.aliucord.patcher.*;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import rx.functions.Action1;
import top.canyie.pine.callback.MethodHook;

@SuppressWarnings({"unused", "deprecation"})
public class PatcherAPI {
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

    private Runnable createUnpatch(XC_MethodHook.Unhook unhook) {
        Runnable unpatch = new Runnable() {
            public void run() {
                unhook.unhook();
                unpatches.remove(this);
            }
        };
        unpatches.add(unpatch);
        return unpatch;
    }


    /**
     * Patches a method.
     *
     * @param forClass   Class to patch.
     * @param fn         Method to patch.
     * @param paramTypes Parameters of the <code>fn</code>. Useful for patching individual overloads.
     * @param hook       Callback for the patch.
     * @return A {@link Runnable} object.
     * @see PinePatchFn
     * @see PinePrePatchFn
     * @deprecated Use {@link #patch(String, String, Class[], XC_MethodHook)}
     */
    @Deprecated
    public Runnable patch(@NonNull String forClass, @NonNull String fn, @NonNull Class<?>[] paramTypes, @NonNull MethodHook hook) {
        return createUnpatch(Patcher.addPatch(forClass, fn, paramTypes, hook));
    }

    /**
     * Patches a method.
     *
     * @param clazz      Class to patch.
     * @param fn         Method to patch.
     * @param paramTypes Parameters of the <code>fn</code>. Useful for patching individual overloads.
     * @param hook       Callback for the patch.
     * @return Method that will remove the patch when invoked
     * @see PinePatchFn
     * @see PinePrePatchFn
     * @deprecated Use {@link #patch(Class, String, Class[], XC_MethodHook)}
     */
    @Deprecated
    public Runnable patch(@NonNull Class<?> clazz, @NonNull String fn, @NonNull Class<?>[] paramTypes, @NonNull MethodHook hook) {
        return createUnpatch(Patcher.addPatch(clazz, fn, paramTypes, hook));
    }

    /**
     * Patches a method or constructor.
     *
     * @param m    Method or constructor to patch. see {@link Member}.
     * @param hook Callback for the patch.
     * @return Method that will remove the patch when invoked
     * @see PatcherAPI#patch(String, String, Class[], MethodHook)
     * @see PatcherAPI#patch(Class, String, Class[], MethodHook)
     * @see PinePatchFn
     * @see PinePrePatchFn
     * @deprecated Use {@link #patch(Member, XC_MethodHook)}
     */
    @Deprecated
    public Runnable patch(@NonNull Member m, @NonNull MethodHook hook) {
        return createUnpatch(Patcher.addPatch(m, hook));
    }


    /**
     * Patches a method.
     *
     * @param forClass   Class to patch.
     * @param fn         Method to patch.
     * @param paramTypes Parameters of the <code>fn</code>. Useful for patching individual overloads.
     * @param hook       Callback for the patch.
     * @return A {@link Runnable} object.
     */
    public Runnable patch(@NonNull String forClass, @NonNull String fn, @NonNull Class<?>[] paramTypes, @NonNull XC_MethodHook hook) {
        return createUnpatch(Patcher.addPatch(forClass, fn, paramTypes, hook));
    }

    /**
     * Patches a method.
     *
     * @param clazz      Class to patch.
     * @param fn         Method to patch.
     * @param paramTypes Parameters of the <code>fn</code>. Useful for patching individual overloads.
     * @param hook       Callback for the patch.
     * @return Method that will remove the patch when invoked
     */
    public Runnable patch(@NonNull Class<?> clazz, @NonNull String fn, @NonNull Class<?>[] paramTypes, @NonNull XC_MethodHook hook) {
        return createUnpatch(Patcher.addPatch(clazz, fn, paramTypes, hook));
    }

    /**
     * Patches a method or constructor.
     *
     * @param m    Method or constructor to patch. see {@link Member}.
     * @param hook Callback for the patch.
     * @return Method that will remove the patch when invoked
     */
    public Runnable patch(@NonNull Member m, @NonNull XC_MethodHook hook) {
        return createUnpatch(Patcher.addPatch(m, hook));
    }

    /**
     * Patches a method or constructor.
     *
     * @param m    Method or constructor to patch. see {@link Member}.
     * @param callback Callback for the patch.
     * @return Method that will remove the patch when invoked
     */
    public Runnable patch(@NonNull Member m, @NonNull Action1<XC_MethodHook.MethodHookParam> callback) {
        return createUnpatch(Patcher.addPatch(m, new Hook(callback)));
    }

    /**
     * Removes all patches.
     */
    public void unpatchAll() {
        Object[] runnables = unpatches.toArray();
        for (Object unpatch : runnables) ((Runnable) unpatch).run();
    }
}
