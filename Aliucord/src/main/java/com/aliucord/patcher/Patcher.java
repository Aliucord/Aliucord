package com.aliucord.patcher;

import com.aliucord.Logger;
import com.aliucord.Main;
import com.aliucord.api.PatcherAPI;
import com.discord.app.AppActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "unused", "ConstantConditions"})
public class Patcher {
    public static Map<String, Map<String, ArrayList<PrePatchFunction>>> prePatches = new HashMap<>();
    public static Map<String, Map<String, ArrayList<PatchFunction<Object>>>> patches = new HashMap<>();
    public static Logger logger = new Logger("Patcher");
    private static int i = 0;

    static {
        String className = "com.discord.app.AppActivity$a";
        String fn = "invoke";
        Patcher.addPatch(className, fn, new PatchFunction<Object>() {
            public Object run(Object __this, ArrayList<Object> args, Object ret) {
                AppActivity.a _this = (AppActivity.a) __this;
                if (i == 1) Main.preInit((AppActivity) _this.i);
                else if (i == 2) {
                    Main.init((AppActivity) _this.i);
                    PatcherAPI.unpatch(className, fn, this);
                }
                i++;
                return ret;
            }
        });
    }

    public static PrePatchRes runPrePatches(Object _this, ArrayList<Object> args) {
        PrePatchRes res = new PrePatchRes(args);
        String[] names = getCallerNames();
        Map<String, ArrayList<PrePatchFunction>> cp = prePatches.get(names[0]);
        if (cp == null) return res;
        ArrayList<PrePatchFunction> p = cp.get(names[1]);
        if (p == null) return res;
        boolean callOriginal = true;
        Object ret = null;
        for (PrePatchFunction patch : p) {
            try {
                PrePatchRes res2 = patch.run(_this, args);
                if (res2 != null) res = res2;
                if (!res.callOriginalMethod) {
                    callOriginal = false;
                    ret = res.ret;
                }
            } catch (Throwable e) {
                logger.error("Failed to run prepatch on " + names[0] + "." + names[1], e);
            }
        }
        res.callOriginalMethod = callOriginal;
        res.ret = ret;
        return res;
    }

    public static <T> T runPatches(Object _this, ArrayList<Object> args, T ret) {
        String[] names = getCallerNames();
//        logger.debug(Arrays.toString(names));
        Map<String, ArrayList<PatchFunction<Object>>> cp = patches.get(names[0]);
        if (cp == null) return ret;
        ArrayList<PatchFunction<Object>> p = cp.get(names[1]);
        if (p == null) return ret;
        try {
            for (PatchFunction<Object> patch : p) ret = (T) patch.run(_this, args, ret);
        } catch (Throwable e) {
            logger.error("Failed to run patch on " + names[0] + "." + names[1], e);
        }
        return ret;
    }

    public static void addPrePatch(String forClass, String fn, PrePatchFunction patch) {
        Map<String, ArrayList<PrePatchFunction>> cp = prePatches.get(forClass);
        if (cp == null) {
            cp = new HashMap<>();
            prePatches.put(forClass, cp);
        }
        ArrayList<PrePatchFunction> p = cp.get(fn);
        if (p == null) {
            p = new ArrayList<>();
            cp.put(fn, p);
        }
        p.add(patch);
    }
    public static void addPatch(String forClass, String fn, PatchFunction<Object> patch) {
        Map<String, ArrayList<PatchFunction<Object>>> cp = patches.get(forClass);
        if (cp == null) {
            cp = new HashMap<>();
            patches.put(forClass, cp);
        }
        ArrayList<PatchFunction<Object>> p = cp.get(fn);
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
