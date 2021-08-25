/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedThumbnail

/**
 * Wraps the obfuscated [EmbedThumbnail] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class ThumbnailWrapper(private val thumbnail: EmbedThumbnail) {
  /** Returns the raw (obfuscated) [EmbedThumbnail] Object associated with this wrapper */
  fun raw() = thumbnail

  val url
    get() = thumbnail.url

  val proxyUrl
    get() = thumbnail.proxyUrl

  val height
    get() = thumbnail.height

  val width
    get() = thumbnail.width

  companion object {
    @JvmStatic
    val EmbedThumbnail.url: String
      get() = c()

    @JvmStatic
    val EmbedThumbnail.proxyUrl: String
      get() = b()

    @JvmStatic
    val EmbedThumbnail.height: Int?
      get() = a()

    @JvmStatic
    val EmbedThumbnail.width: Int?
      get() = d()
  }
}
