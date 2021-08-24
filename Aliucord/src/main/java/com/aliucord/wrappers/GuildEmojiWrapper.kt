/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticGuildEmojiWrapper")
package com.aliucord.wrappers

import com.discord.api.emoji.GuildEmoji

@get:JvmName("isAnimated")
val GuildEmoji.animated
  get() = a()

@get:JvmName("isAvailable")
val GuildEmoji.available: Boolean?
  get() = b()

val GuildEmoji.id
  get() = c()

@get:JvmName("isManaged")
val GuildEmoji.managed
  get() = d()

val GuildEmoji.name: String
  get() = e()

val GuildEmoji.requireColons
  get() = f()

val GuildEmoji.roles: List<Long>
  get() = g()

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
    @Deprecated(
      "For java: use StaticGuildEmojiWrapper.isAnimated\nFor kotlin: use emoji.animated",
      ReplaceWith("emoji.animated"),
      DeprecationLevel.ERROR,
    )
    fun isAnimated(emoji: GuildEmoji) = emoji.animated

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildEmojiWrapper.isAvailable\nFor kotlin: use emoji.available",
      ReplaceWith("emoji.available"),
      DeprecationLevel.ERROR,
    )
    fun isAvailable(emoji: GuildEmoji) = emoji.available

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildEmojiWrapper.getId\nFor kotlin: use emoji.id",
      ReplaceWith("emoji.id"),
      DeprecationLevel.ERROR,
    )
    fun getId(emoji: GuildEmoji) = emoji.id

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildEmojiWrapper.isManaged\nFor kotlin: use emoji.managed",
      ReplaceWith("emoji.managed"),
      DeprecationLevel.ERROR,
    )
    fun isManaged(emoji: GuildEmoji) = emoji.managed

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildEmojiWrapper.isAnimated\nFor kotlin: use emoji.animated",
      ReplaceWith("emoji.animated"),
      DeprecationLevel.ERROR,
    )
    fun getName(emoji: GuildEmoji) = emoji.name

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildEmojiWrapper.requireColons\nFor kotlin: use emoji.requireColons",
      ReplaceWith("emoji.requireColons"),
      DeprecationLevel.ERROR,
    )
    fun requireColons(emoji: GuildEmoji) = emoji.requireColons

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildEmojiWrapper.getRoles\nFor kotlin: use emoji.roles",
      ReplaceWith("emoji.roles"),
      DeprecationLevel.ERROR,
    )
    fun getRoles(emoji: GuildEmoji) = emoji.roles
  }
}
