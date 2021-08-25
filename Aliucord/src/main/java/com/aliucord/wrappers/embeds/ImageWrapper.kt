/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticImageWrapper")
package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedImage

val EmbedImage.url: String
  get() = c()

val EmbedImage.proxyUrl: String
  get() = b()

val EmbedImage.height: Int?
  get() = a()

val EmbedImage.width: Int?
  get() = d()

/**
 * Wraps the obfuscated [EmbedImage] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class ImageWrapper(private val image: EmbedImage) {
  /** Returns the raw (obfuscated) [EmbedImage] Object associated with this wrapper */
  fun raw() = image

  val url
    get() = image.url

  val proxyUrl
    get() = image.proxyUrl

  val height
    get() = image.height

  val width
    get() = image.width

  companion object {
    @JvmStatic
    @Deprecated(
      "For java: use StaticImageWrapper.getUrl\nFor kotlin: use image.url",
      ReplaceWith("image.url"),
      DeprecationLevel.ERROR,
    )
    fun getUrl(image: EmbedImage) = image.url

    @JvmStatic
    @Deprecated(
      "For java: use StaticImageWrapper.getProxyUrl\nFor kotlin: use image.proxyUrl",
      ReplaceWith("image.proxyUrl"),
      DeprecationLevel.ERROR,
    )
    fun getProxyUrl(image: EmbedImage) = image.proxyUrl

    @JvmStatic
    @Deprecated(
      "For java: use StaticImageWrapper.getHeight\nFor kotlin: use image.height",
      ReplaceWith("image.height"),
      DeprecationLevel.ERROR,
    )
    fun getHeight(image: EmbedImage) = image.height

    @JvmStatic
    @Deprecated(
      "For java: use StaticImageWrapper.getWidth\nFor kotlin: use image.width",
      ReplaceWith("image.width"),
      DeprecationLevel.ERROR,
    )
    fun getWidth(image: EmbedImage) = image.width
  }
}
