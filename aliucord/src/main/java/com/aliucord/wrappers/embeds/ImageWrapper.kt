/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedImage

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
    val EmbedImage.url: String
      get() = c()

    @JvmStatic
    val EmbedImage.proxyUrl: String
      get() = b()

    @JvmStatic
    val EmbedImage.height: Int?
      get() = a()

    @JvmStatic
    val EmbedImage.width: Int?
      get() = d()
  }
}
