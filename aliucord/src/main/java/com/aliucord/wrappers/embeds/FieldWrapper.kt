/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.EmbedField

/**
 * Wraps the obfuscated [EmbedField] class to provide nice method names and require only one central
 * update if method names change after an update
 */
class FieldWrapper(private val field: EmbedField) {
  /** Returns the raw (obfuscated) [EmbedField] Object associated with this wrapper  */
  fun raw() = field

  val name
    get() = field.name

  val value
    get() = field.value

  @get:JvmName("isInline")
  val inline
    get() = field.inline

  companion object {
    @JvmStatic
    val EmbedField.name: String
      get() = a()

    @JvmStatic
    val EmbedField.value: String
      get() = b()

    private val inlineField = EmbedField::class.java.getDeclaredField("inline").apply { isAccessible = true }

    @JvmStatic
    @get:JvmName("isInline")
    val EmbedField.inline
      get() = inlineField[this] as Boolean? == java.lang.Boolean.TRUE
  }
}
