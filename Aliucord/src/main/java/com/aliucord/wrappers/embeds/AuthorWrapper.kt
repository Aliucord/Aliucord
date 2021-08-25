/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedAuthor

/**
 * Wraps the obfuscated [EmbedAuthor] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class AuthorWrapper(private val author: EmbedAuthor) {
  /** Returns the raw (obfuscated) [EmbedAuthor] Object associated with this wrapper */
  fun raw() = author

  val name
    get() = author.name

  val proxyIconUrl
    get() = author.proxyIconUrl

  val url
    get() = author.url

  companion object {
    @JvmStatic
    val EmbedAuthor.name: String
      get() = a()

    @JvmStatic
    val EmbedAuthor.proxyIconUrl: String?
      get() = b()

    @JvmStatic
    val EmbedAuthor.url: String?
      get() = c()
  }
}
