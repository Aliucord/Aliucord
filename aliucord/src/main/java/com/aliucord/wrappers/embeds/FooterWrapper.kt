/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedFooter

/**
 * Wraps the obfuscated [EmbedFooter] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class FooterWrapper(private val footer: EmbedFooter) {
  /** Returns the raw (obfuscated) [EmbedFooter] Object associated with this wrapper  */
  fun raw() = footer

  val text
    get() = footer.text

  val iconUrl
    get() = footer.iconUrl

  val proxyIconUrl
    get() = footer.proxyIconUrl

  companion object {
    @JvmStatic
    val EmbedFooter.text: String
      get() = b()

    @JvmStatic
    val EmbedFooter.proxyIconUrl: String?
      get() = a()

    // why is there no getter for this lol
    // FIXME: Do this without reflection once Discord adds getter
    private val iconUrlField = EmbedFooter::class.java.getDeclaredField("iconUrl").apply { isAccessible = true }

    @JvmStatic
    val EmbedFooter.iconUrl
      get() = iconUrlField[this] as String?
  }
}
