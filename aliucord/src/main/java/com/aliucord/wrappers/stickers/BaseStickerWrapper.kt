/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.stickers

import com.discord.api.sticker.*

/**
 * Wraps the obfuscated [BaseSticker] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class BaseStickerWrapper(private val sticker: BaseSticker) {
  /** Returns the raw (obfuscated) [BaseSticker] Object associated with this wrapper */
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
    val BaseSticker.formatType: StickerFormatType
      get() = a()

    @JvmStatic
    val BaseSticker.format: String
      get() = b()

    @JvmStatic
    val BaseSticker.stickerPartial: StickerPartial
      get() = c()

    @JvmStatic
    val BaseSticker.id: Long
      get() = d()
  }
}
