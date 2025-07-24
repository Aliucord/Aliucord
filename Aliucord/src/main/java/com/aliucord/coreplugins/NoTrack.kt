/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
// based on https://gitdab.com/distok/cutthecord/src/branch/master/patches/notrack/1371.patch
package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.InsteadHook
import com.aliucord.patcher.InsteadHook.Companion.returnConstant
import com.aliucord.patcher.Patcher
import com.discord.utilities.surveys.SurveyUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import de.robv.android.xposed.XposedBridge

internal class NoTrack : CorePlugin(Manifest("NoTrack")) {
    init {
        manifest.description = "Disables certain various app analytics and tracking"
    }

    override val isRequired = true

    @OptIn(ExperimentalStdlibApi::class)
    override fun load(context: Context) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)

        val cl = NoTrack::class.java.classLoader!!

        buildMap<String, Array<String>> {
            // com.google.firebase.crashlytics.internal.common.CommonUtils getMappingFileId
            // https://github.com/firebase/firebase-android-sdk/blob/master/firebase-crashlytics/src/main/java/com/google/firebase/crashlytics/internal/common/CommonUtils.java#L582
            put("b.i.c.m.d.k.h", arrayOf("l"))
            put("b.i.a.f.i.b.k9", arrayOf("n", "Q"))
            put("b.i.a.b.j.t.h.g", arrayOf("run"))
            put("com.discord.stores.StoreUserSurvey", arrayOf("handleConnectionOpen"))
            put("com.discord.utilities.analytics.AdjustConfig", arrayOf("init"))
            put(
                "com.discord.utilities.analytics.AdjustConfig\$AdjustLifecycleListener",
                arrayOf("onActivityPaused", "onActivityResumed")
            )
            put("com.discord.utilities.analytics.AnalyticsTracker\$AdjustEventTracker", arrayOf("trackLogin", "trackRegister"))
            put("com.discord.utilities.analytics.AnalyticSuperProperties", arrayOf("setCampaignProperties"))
            put(
                "com.discord.utilities.analytics.AnalyticsUtils\$Tracker",
                arrayOf("drainEventsQueue", "setTrackingData", "track", "trackFireBase")
            )
            put("com.discord.utilities.integrations.SpotifyHelper\$openPlayStoreForSpotify$1", arrayOf("run"))
        }.forEach { (key, functions) ->
            val clazz = cl.loadClass(key)
            functions.forEach { name ->
                XposedBridge.hookAllMethods(clazz, name, InsteadHook.DO_NOTHING)
            }
        }

        Patcher.addPatch(SurveyUtils::class.java.getDeclaredMethod("isInstallOldEnough"), returnConstant(false))
    }

    override fun start(context: Context) {}

    override fun stop(context: Context) {}
}
