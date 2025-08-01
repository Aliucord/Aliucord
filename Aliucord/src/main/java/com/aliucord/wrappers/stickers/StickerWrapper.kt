/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.stickers

import com.discord.api.sticker.*

/**
 * Wraps the obfuscated [Sticker] class to provide nice method names
 */
@Suppress("unused")
class StickerWrapper(private val sticker: Sticker) {
  /** Returns the raw (obfuscated) [Sticker] Object associated with this wrapper */
  fun raw() = sticker

  val formatType
    get() = sticker.formatType

  val format
    get() = sticker.format

  val stickerPartial
    get() = sticker.stickerPartial

  val id
    get() = sticker.id

  val available
    get() = sticker.available

  val description
    get() = sticker.description

  val guildId
    get() = sticker.guildId

  val name
    get() = sticker.name

  val packId
    get() = sticker.packId

  val tags
    get() = sticker.tags

  val type
    get() = sticker.type

  val animated
    get() = sticker.animated //it doesn't have a name actually but that's what it does

  companion object {
    @JvmStatic
    val Sticker.formatType: StickerFormatType
      get() = a()

    @JvmStatic
    val Sticker.format: String
      get() = b()

    @JvmStatic
    val Sticker.stickerPartial: StickerPartial
      get() = c()

    @JvmStatic
    val Sticker.id: Long
      get() = d()

    @JvmStatic
    val Sticker.available: Boolean?
      get() = e()

    @JvmStatic
    val Sticker.description: String
      get() = f()

    @JvmStatic
    val Sticker.guildId: Long
      get() = g()

    @JvmStatic
    val Sticker.name: String
      get() = h()

    @JvmStatic
    val Sticker.packId: Long
      get() = i()

    @JvmStatic
    val Sticker.tags: String
      get() = j()

    @JvmStatic
    val Sticker.type: StickerType
      get() = k()

    @JvmStatic
    val Sticker.animated: Boolean
      get() = l()
  }
}
