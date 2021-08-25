/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedProvider

/**
 * Wraps the obfuscated [EmbedProvider] class to provide nice method names and require only one central
 * update if method names change after an update
 */
class ProviderWrapper(private val provider: EmbedProvider) {
  /** Returns the raw (obfuscated) [EmbedProvider] Object associated with this wrapper */
  fun raw() = provider

  val name
    get() = provider.name

  val url
    get() = provider.url

  companion object {
    @JvmStatic
    val EmbedProvider.name: String
      get() = a()

    @JvmStatic
    val EmbedProvider.url: String?
      get() = b()
  }
}
