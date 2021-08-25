/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedVideo

/**
 * Wraps the obfuscated [EmbedVideo] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class VideoWrapper(private val video: EmbedVideo) {
  /** Returns the raw (obfuscated) [EmbedVideo] Object associated with this wrapper */
  fun raw() = video

  val url
    get() = video.url

  val proxyUrl
    get() = video.proxyUrl

  val height
    get() = video.height

  val width
    get() = video.width

  companion object {
    @JvmStatic
    val EmbedVideo.url: String
      get() = c()

    @JvmStatic
    val EmbedVideo.proxyUrl: String
      get() = b()

    @JvmStatic
    val EmbedVideo.height: Int?
      get() = a()

    @JvmStatic
    val EmbedVideo.width: Int?
      get() = d()
  }
}
