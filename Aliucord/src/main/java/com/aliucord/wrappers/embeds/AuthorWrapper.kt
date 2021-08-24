/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticAuthorWrapper")
package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedAuthor

val EmbedAuthor.name: String
  get() = a()

val EmbedAuthor.proxyIconUrl: String?
  get() = b()

val EmbedAuthor.url: String?
  get() = c()

/**
 * Wraps the obfuscated [EmbedAuthor] class to provide nice method names and require only one central
 * update if method names change after an update
 */
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
    @Deprecated(
      "For java: use StaticAuthorWrapper.getName\nFor kotlin: use author.name",
      ReplaceWith("author.name"),
      DeprecationLevel.ERROR,
    )
    fun getName(author: EmbedAuthor) = author.name

    @JvmStatic
    @Deprecated(
      "For java: use StaticAuthorWrapper.getProxyIconUrl\nFor kotlin: use author.proxyIconUrl",
      ReplaceWith("author.proxyIconUrl"),
      DeprecationLevel.ERROR,
    )
    fun getProxyIconUrl(author: EmbedAuthor) = author.proxyIconUrl

    @JvmStatic
    @Deprecated(
      "For java: use StaticAuthorWrapper.getUrl\nFor kotlin: use author.url",
      ReplaceWith("author.url"),
      DeprecationLevel.ERROR,
    )
    fun getUrl(author: EmbedAuthor) = author.url
  }
}
