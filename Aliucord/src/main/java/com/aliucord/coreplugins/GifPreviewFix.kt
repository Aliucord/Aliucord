/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import android.net.Uri
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.utilities.embed.EmbedResourceUtils

internal class GifPreviewFix : Plugin(Manifest("GifPreviewFix")) {
    override fun load(context: Context) {
        patcher.after<EmbedResourceUtils>("getPreviewUrls", String::class.java, Int::class.java, Int::class.java, Boolean::class.java) {
            // return List<String>
            var result = (it.result as List<String>).toMutableList()

            val uri = Uri.parse(result[0])
            if (uri.path?.endsWith(".gif") == true) {
                val newUri = uri.buildUpon().encodedQuery("format=gif").build()
                result[0] = newUri.toString()

                it.result = result
            }
        }
    }

    override fun start(context: Context?) {}

    override fun stop(context: Context?) {}
}
