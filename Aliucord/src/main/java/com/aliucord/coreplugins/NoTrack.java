/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

// based on https://gitdab.com/distok/cutthecord/src/branch/master/patches/notrack/1371.patch

package com.aliucord.coreplugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.CollectionUtils;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;

import java.lang.reflect.Method;
import java.util.*;

import top.canyie.pine.callback.MethodReplacement;

public final class NoTrack extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() { return new Manifest(); }

    @Override
    public void load(Context context) throws Throwable {
        Map<String, String[]> map = new HashMap<>();

        // com.google.firebase.crashlytics.internal.common.CommonUtils getMappingFileId
        // https://github.com/firebase/firebase-android-sdk/blob/master/firebase-crashlytics/src/main/java/com/google/firebase/crashlytics/internal/common/CommonUtils.java#L582
        map.put("c.i.c.m.d.k.h", new String[]{ "l" });

        map.put("c.i.a.f.i.b.k9", new String[]{ "n", "Q" });
        map.put("c.i.a.b.j.t.h.g", new String[]{ "run" });
        // map.put("c.i.a.f.h.i.r", Collections.singletonList("R"));
        map.put("com.discord.utilities.analytics.AdjustConfig", new String[]{ "init" });
        map.put("com.discord.utilities.analytics.AdjustConfig$AdjustLifecycleListener", new String[]{ "onActivityPaused", "onActivityResumed" });
        map.put("com.discord.utilities.analytics.AnalyticsTracker$AdjustEventTracker", new String[]{ "trackLogin", "trackRegister" });
        map.put("com.discord.utilities.analytics.AnalyticSuperProperties", new String[]{ "setCampaignProperties" });
        map.put("com.discord.utilities.analytics.AnalyticsUtils$Tracker", new String[]{ "drainEventsQueue", "setTrackingData", "track", "trackFireBase" });
        map.put("com.discord.utilities.integrations.SpotifyHelper$openPlayStoreForSpotify$1", new String[]{ "run" });

        final MethodReplacement patch = MethodReplacement.returnConstant(null);
        final ClassLoader cl = Objects.requireNonNull(NoTrack.class.getClassLoader());

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String className = entry.getKey();
            Method[] methods = cl.loadClass(className).getDeclaredMethods();
            for (String fn : entry.getValue()) {
                Method m = CollectionUtils.find(Arrays.asList(methods), method -> method.getName().equals(fn));
                if (m != null) Patcher.addPatch(m, patch);
            }
        }
    }

    @Override
    public void start(Context context) {}

    @Override
    public void stop(Context context) {}
}
