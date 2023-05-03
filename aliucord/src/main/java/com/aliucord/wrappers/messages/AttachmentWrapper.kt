/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.messages

import com.discord.api.message.attachment.MessageAttachment
import com.discord.api.message.attachment.MessageAttachmentType

/**
 * Wraps the obfuscated [MessageAttachment] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class AttachmentWrapper(private val attachment: MessageAttachment) {
  /** Returns the raw (obfuscated) [MessageAttachment] Object associated with this wrapper */
  fun raw() = attachment

  val filename
    get() = attachment.filename

  val height
    get() = attachment.height

  val proxyUrl
    get() = attachment.proxyUrl

  val size
    get() = attachment.size

  val type
    get() = attachment.type

  val url
    get() = attachment.url

  val width
    get() = attachment.width

  companion object {
    @JvmStatic
    val MessageAttachment.filename: String
      get() = a()

    @JvmStatic
    val MessageAttachment.height: Int?
      get() = b()

    @JvmStatic
    val MessageAttachment.proxyUrl: String
      get() = c()

    @JvmStatic
    val MessageAttachment.size: Long
      get() = d()

    @JvmStatic
    val MessageAttachment.type: MessageAttachmentType
      get() = e()

    @JvmStatic
    val MessageAttachment.url: String
      get() = f()

    @JvmStatic
    val MessageAttachment.width: Int?
      get() = g()
  }
}
