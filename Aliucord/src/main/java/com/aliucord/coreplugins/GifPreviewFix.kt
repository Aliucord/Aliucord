/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import android.net.Uri
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.utilities.embed.EmbedResourceUtils

internal class GifPreviewFix : CorePlugin(Manifest("GifPreviewFix")) {
    override val isHidden = true
    override val isRequired = true

    override fun load(context: Context) {
        patcher.after<EmbedResourceUtils>(
            "getPreviewUrls",
            String::class.java, Int::class.java, Int::class.java, Boolean::class.java,
        ) { (params, _: String, _: Int, _: Int, animated: Boolean) ->
            if (!animated) return@after

            @Suppress("UNCHECKED_CAST")
            val urls = (params.result as List<String>).toMutableList()

            val uri = Uri.parse(urls[0].replace("&?", "&"))
                ?.takeIf { it.path?.endsWith(".gif") == true }
                ?: return@after

            val filteredQueryKeys = uri.queryParameterNames.filter { it != "format" }

            val newUri = uri.buildUpon()
                .clearQuery()
                .apply { filteredQueryKeys.forEach { appendQueryParameter(it, uri.getQueryParameter(it)) } }
                .build()

            urls[0] = newUri.toString()
            params.result = urls
        }
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}
}
