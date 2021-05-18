// based on https://gitdab.com/distok/cutthecord/src/branch/master/patches/notrack/1371.patch

package com.aliucord.coreplugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;

import java.util.*;

public class NoTrack extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() { return new Manifest(); }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();

        // com.google.firebase.crashlytics.internal.common.CommonUtils getMappingFileId
        // https://github.com/firebase/firebase-android-sdk/blob/master/firebase-crashlytics/src/main/java/com/google/firebase/crashlytics/internal/common/CommonUtils.java#L582
        map.put("c.i.c.m.d.k.h", Collections.singletonList("l"));

        map.put("c.i.a.f.i.b.k9", Arrays.asList("n", "Q"));
        map.put("c.i.a.b.j.t.h.g", Collections.singletonList("run"));
        map.put("c.i.a.f.h.i.r", Collections.singletonList("R"));
        map.put("com.discord.utilities.analytics.AdjustConfig", Collections.singletonList("init"));
        map.put("com.discord.utilities.analytics.AdjustConfig$AdjustLifecycleListener", Arrays.asList("onActivityPaused", "onActivityResumed"));
//        map.put("com.discord.utilities.analytics.AnalyticsTracker$AdjustEventTracker", Collections.singletonList("*"));
        map.put("com.discord.utilities.analytics.AnalyticsTracker$AdjustEventTracker", Arrays.asList("trackLogin", "trackRegister"));
        map.put("com.discord.utilities.analytics.AnalyticSuperProperties", Arrays.asList("setSuperProperties", "setCampaignProperties"));
//        map.put("com.discord.utilities.analytics.AnalyticsUtils$Tracker", Collections.singletonList("*"));
        map.put("com.discord.utilities.analytics.AnalyticsUtils$Tracker", Arrays.asList("drainEventsQueue", "setTrackingData", "track", "trackFireBase"));
        map.put("com.discord.utilities.integrations.SpotifyHelper$openPlayStoreForSpotify$1", Collections.singletonList("run"));
        return map;
    }

    @Override
    public void load(Context context) {
        final PrePatchFunction patch = (_this, args) -> new PrePatchRes(null);

        for (Map.Entry<String, List<String>> entry : getClassesToPatch().entrySet()) {
            String className = entry.getKey();
            if (className.equals("com.discord.utilities.analytics.AnalyticSuperProperties")) continue;

            for (String fn : entry.getValue()) Patcher.addPrePatch(className, fn, patch);
        }

        String className = "com.discord.utilities.analytics.AnalyticSuperProperties";
        final String[] whitelist = new String[]{ "browser", "browser_user_agent", "os", "accessibility_support_enabled", "accessibility_features" };
        Patcher.addPrePatch(className, "setSuperProperties", (_this, args) -> {
            @SuppressWarnings("unchecked") Map<String, Object> props = (Map<String, Object>) args.get(0);
            Map<String, Object> newProps = new HashMap<>();
            for (String key : whitelist) {
                Object val = props.get(key);
                if (val != null) newProps.put(key, val);
            }
            if (newProps.size() == 0) return new PrePatchRes(null);
            args.set(0, newProps);
            return null;
        });
        Patcher.addPrePatch(className, "setCampaignProperties", patch);
    }

    @Override
    public void start(Context context) {}

    @Override
    public void stop(Context context) {}
}
