/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers

import com.discord.api.emoji.GuildEmoji

/**
 * Wraps the obfuscated [GuildEmoji] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class GuildEmojiWrapper(private val emoji: GuildEmoji) {
  /** Returns the raw (obfuscated) [GuildEmoji] Object associated with this wrapper */
  fun raw() = emoji

  @get:JvmName("isAnimated")
  val animated
    get() = emoji.animated

  @get:JvmName("isAvailable")
  val available
    get() = emoji.available

  val id
    get() = emoji.id

  @get:JvmName("isManaged")
  val managed
    get() = emoji.managed

  val name
    get() = emoji.name

  val roles
    get() = emoji.roles

  companion object {
    @JvmStatic
    @get:JvmName("isAnimated")
    val GuildEmoji.animated
      get() = a()

    @JvmStatic
    @get:JvmName("isAvailable")
    val GuildEmoji.available: Boolean?
      get() = b()

    @JvmStatic
    val GuildEmoji.id
      get() = c()

    @JvmStatic
    @get:JvmName("isManaged")
    val GuildEmoji.managed
      get() = d()

    @JvmStatic
    val GuildEmoji.name: String
      get() = e()

    @JvmStatic
    val GuildEmoji.requireColons
      get() = f()

    @JvmStatic
    val GuildEmoji.roles: List<Long>
      get() = g()
  }
}
