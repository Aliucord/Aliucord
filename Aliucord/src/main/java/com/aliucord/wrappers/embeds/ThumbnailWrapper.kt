/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticThumbnailWrapper")
package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedThumbnail

val EmbedThumbnail.url: String
  get() = c()

val EmbedThumbnail.proxyUrl: String
  get() = b()

val EmbedThumbnail.height: Int?
  get() = a()

val EmbedThumbnail.width: Int?
  get() = d()

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
    @Deprecated(
      "For java: use StaticThumbnailWrapper.getUrl\nFor kotlin: use thumbnail.url",
      ReplaceWith("thumbnail.url"),
      DeprecationLevel.ERROR,
    )
    fun getUrl(thumbnail: EmbedThumbnail) = thumbnail.url

    @JvmStatic
    @Deprecated(
      "For java: use StaticThumbnailWrapper.getProxyUrl\nFor kotlin: use thumbnail.proxyUrl",
      ReplaceWith("thumbnail.proxyUrl"),
      DeprecationLevel.ERROR,
    )
    fun getProxyUrl(thumbnail: EmbedThumbnail) = thumbnail.proxyUrl

    @JvmStatic
    @Deprecated(
      "For java: use StaticThumbnailWrapper.getHeight\nFor kotlin: use thumbnail.height",
      ReplaceWith("thumbnail.height"),
      DeprecationLevel.ERROR,
    )
    fun getHeight(thumbnail: EmbedThumbnail) = thumbnail.height

    @JvmStatic
    @Deprecated(
      "For java: use StaticThumbnailWrapper.getWidth\nFor kotlin: use thumbnail.width",
      ReplaceWith("thumbnail.width"),
      DeprecationLevel.ERROR,
    )
    fun getWidth(thumbnail: EmbedThumbnail) = thumbnail.width
  }
}
