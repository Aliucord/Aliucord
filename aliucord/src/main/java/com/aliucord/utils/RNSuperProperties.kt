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
    val superProperties = JSONObject().apply {
        put("os", "Android")
        put("browser", "Discord Android")
        put("device", Build.DEVICE)
        put("system_locale", Locale.getDefault().toString().replace("_", "-"))
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

    // update to latest Beta branch sometimes
    const val versionCode = 176120
    const val versionString = "176.20 - rn"
    const val userAgent = "Discord-Android/$versionCode;RNA"
}
