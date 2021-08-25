/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticBaseStickerWrapper")
package com.aliucord.wrappers.stickers

import com.discord.api.sticker.*

val BaseSticker.formatType: StickerFormatType
  get() = a()

val BaseSticker.format: String
  get() = b()

val BaseSticker.stickerPartial: StickerPartial
  get() = c()

val BaseSticker.id: Long
  get() = d()

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
    @Deprecated(
      "For java: use StaticBaseStickerWrapper.getFormatType\nFor kotlin: use sticker.formatType",
      ReplaceWith("sticker.formatType"),
      DeprecationLevel.ERROR,
    )
    fun getFormatType(sticker: BaseSticker) = sticker.formatType

    @JvmStatic
    @Deprecated(
      "For java: use StaticBaseStickerWrapper.getFormat\nFor kotlin: use sticker.format",
      ReplaceWith("sticker.format"),
      DeprecationLevel.ERROR,
    )
    fun getFormat(sticker: BaseSticker) = sticker.format

    @JvmStatic
    @Deprecated(
      "For java: use StaticBaseStickerWrapper.getStickerPartial\nFor kotlin: use sticker.stickerPartial",
      ReplaceWith("sticker.stickerPartial"),
      DeprecationLevel.ERROR,
    )
    fun getStickerPartial(sticker: BaseSticker) = sticker.stickerPartial

    @JvmStatic
    @Deprecated(
      "For java: use StaticBaseStickerWrapper.getId\nFor kotlin: use sticker.id",
      ReplaceWith("sticker.id"),
      DeprecationLevel.ERROR,
    )
    fun getId(sticker: BaseSticker) = sticker.id
  }
}
