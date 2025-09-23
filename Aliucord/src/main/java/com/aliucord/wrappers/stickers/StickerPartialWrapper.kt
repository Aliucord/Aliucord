/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.stickers

import com.discord.api.sticker.*

/**
 * Wraps the obfuscated [StickerPartial] class to provide nice method names
 */
@Suppress("unused")
class StickerPartialWrapper(private val sticker: StickerPartial) {
  /** Returns the raw (obfuscated) [StickerPartial] Object associated with this wrapper */
  fun raw() = sticker

  val formatType
    get() = sticker.formatType

  val format
    get() = sticker.format

  val stickerPartial
    get() = sticker.stickerPartial

  val id
    get() = sticker.id

  companion object {
    @JvmStatic
    val StickerPartial.formatType: StickerFormatType
      get() = a()

    @JvmStatic
    val StickerPartial.format: String
      get() = b()

    @JvmStatic
    val StickerPartial.stickerPartial: StickerPartial
      get() = c()

    @JvmStatic
    val StickerPartial.id: Long
      get() = d()

    @JvmStatic
    val StickerPartial.name: String
      get() = e()
  }
}
