/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

// based on https://gitdab.com/distok/cutthecord/src/branch/master/patches/notrack/1371.patch

package com.aliucord.coreplugins;

import android.content.Context;

import com.aliucord.entities.CorePlugin;
import com.aliucord.patcher.InsteadHook;
import com.aliucord.patcher.Patcher;
import com.discord.utilities.surveys.SurveyUtils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.*;

import de.robv.android.xposed.XposedBridge;

public final class NoTrack extends CorePlugin {
    public NoTrack() {
        super(new Manifest("NoTrack") {{
            description = "Disables certain various app analytics and tracking";
        }});
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public void load(Context context) throws Throwable {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);

        Map<String, String[]> map = new HashMap<>();

        // com.google.firebase.crashlytics.internal.common.CommonUtils getMappingFileId
        // https://github.com/firebase/firebase-android-sdk/blob/master/firebase-crashlytics/src/main/java/com/google/firebase/crashlytics/internal/common/CommonUtils.java#L582
        map.put("b.i.c.m.d.k.h", new String[]{ "l" });

        map.put("b.i.a.f.i.b.k9", new String[]{ "n", "Q" });
        map.put("b.i.a.b.j.t.h.g", new String[]{ "run" });
        map.put("com.discord.stores.StoreUserSurvey", new String[]{ "handleConnectionOpen" });
        map.put("com.discord.utilities.analytics.AdjustConfig", new String[]{ "init" });
        map.put("com.discord.utilities.analytics.AdjustConfig$AdjustLifecycleListener", new String[]{ "onActivityPaused", "onActivityResumed" });
        map.put("com.discord.utilities.analytics.AnalyticsTracker$AdjustEventTracker", new String[]{ "trackLogin", "trackRegister" });
        map.put("com.discord.utilities.analytics.AnalyticSuperProperties", new String[]{ "setCampaignProperties" });
        map.put("com.discord.utilities.analytics.AnalyticsUtils$Tracker", new String[]{ "drainEventsQueue", "setTrackingData", "track", "trackFireBase" });
        map.put("com.discord.utilities.integrations.SpotifyHelper$openPlayStoreForSpotify$1", new String[]{ "run" });

        final ClassLoader cl = Objects.requireNonNull(NoTrack.class.getClassLoader());

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            var clazz = cl.loadClass(entry.getKey());
            for (String fn : entry.getValue()) {
                XposedBridge.hookAllMethods(clazz, fn, InsteadHook.DO_NOTHING);
            }
        }

        Patcher.addPatch(SurveyUtils.class.getDeclaredMethod("isInstallOldEnough"), InsteadHook.returnConstant(false));
    }

    @Override
    public void start(Context context) {}

    @Override
    public void stop(Context context) {}
}
