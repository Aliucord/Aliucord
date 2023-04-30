/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils

import android.os.Build
import android.util.Base64
import com.aliucord.Main
import com.discord.utilities.analytics.AnalyticSuperProperties
import org.json.JSONObject
import java.util.UUID

@Suppress("MemberVisibilityCanBePrivate")
object RNSuperProperties {
    @JvmStatic
    val superProperties = JSONObject(AnalyticSuperProperties.INSTANCE.superProperties).apply {
        remove("client_performance_cpu")
        remove("client_performance_memory")
        remove("cpu_core_count")
        remove("accessibility_features")
        remove("os_sdk_version")
        remove("accessibility_support_enabled")

        put("device", Build.DEVICE)
        put("client_version", versionString)
        put("release_channel", "betaRelease")
        put("device_vendor_id", vendorId)
        put("browser_user_agent", "")
        put("browser_version", "")
        put("os_version", Build.VERSION.SDK_INT.toString())
        put("client_build_number", versionCode)
        put("client_event_source", JSONObject.NULL)
        put("design_id", 0)
    }

    @JvmStatic
    val superPropertiesBase64: String = Base64.encodeToString(superProperties.toString().toByteArray(), 2)

    // vendorId is a random UUID in normal Discord RN
    @JvmStatic
    val vendorId = Main.settings.getString("rnVendorId", null) ?: UUID.randomUUID().toString().also {
        Main.settings.setString("rnVendorId", it)
    }

    // update to latest Beta branch sometimes
    const val versionCode = 176120
    const val versionString = "176.20 - rn"
}
