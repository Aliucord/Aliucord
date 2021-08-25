/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticVideoWrapper")
package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedVideo

val EmbedVideo.url: String
  get() = c()

val EmbedVideo.proxyUrl: String
  get() = b()

val EmbedVideo.height: Int?
  get() = a()

val EmbedVideo.width: Int?
  get() = d()

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
    @Deprecated(
      "For java: use StaticVideoWrapper.getUrl\nFor kotlin: use video.url",
      ReplaceWith("video.url"),
      DeprecationLevel.ERROR,
    )
    fun getUrl(video: EmbedVideo) = video.url

    @JvmStatic
    @Deprecated(
      "For java: use StaticVideoWrapper.getProxyUrl\nFor kotlin: use video.proxyUrl",
      ReplaceWith("video.proxyUrl"),
      DeprecationLevel.ERROR,
    )
    fun getProxyUrl(video: EmbedVideo) = video.proxyUrl

    @JvmStatic
    @Deprecated(
      "For java: use StaticVideoWrapper.getHeight\nFor kotlin: use video.height",
      ReplaceWith("video.height"),
      DeprecationLevel.ERROR,
    )
    fun getHeight(video: EmbedVideo) = video.height

    @JvmStatic
    @Deprecated(
      "For java: use StaticVideoWrapper.getWidth\nFor kotlin: use video.width",
      ReplaceWith("video.width"),
      DeprecationLevel.ERROR,
    )
    fun getWidth(video: EmbedVideo) = video.width
  }
}
