/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils

import android.os.Build
import android.util.Base64
import com.aliucord.Main
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

@Suppress("MemberVisibilityCanBePrivate")
object RNSuperProperties {
    // vendorId is a random UUID even in normal Discord RN
    @JvmStatic
    val vendorId = Main.settings.getString("rnVendorId", null) ?: UUID.randomUUID().toString().also {
        Main.settings.setString("rnVendorId", it)
    }

    @JvmStatic
    val launchId = UUID.randomUUID().toString()

    @JvmStatic
    val heartbeatSessionId = UUID.randomUUID().toString()

    @JvmStatic
    val launchSignature: String = (System.currentTimeMillis() * 1_000_000L).toString()

    @JvmStatic
    val superProperties = JSONObject().apply {
        put("os", "Android")
        put("browser", "Discord Android")
        put("device", Build.DEVICE)
        put("system_locale", Locale.getDefault().toString().replace("_", "-"))
        put("has_client_mods", false)
        put("client_version", versionString)
        put("release_channel", "canaryRelease")
        put("device_vendor_id", vendorId)
        put("design_id", 2)
        put("browser_user_agent", "")
        put("browser_version", "")
        put("os_version", Build.VERSION.SDK_INT.toString())
        put("client_build_number", buildNumber)
        put("client_event_source", JSONObject.NULL)
        put("client_launch_id", launchId)
        put("launch_signature", launchSignature)
        put("client_app_state", "active")
        put("client_heartbeat_session_id", heartbeatSessionId)
    }

    @JvmStatic
    val superPropertiesBase64: String = Base64.encodeToString(superProperties.toString().toByteArray(), 2)

    // update to latest Beta branch sometimes
    const val buildNumber = 6081
    const val versionCode = 341200
    const val versionString = "341.0 - rn"
    const val userAgent = "Discord-Android/$versionCode;RNA"
}
