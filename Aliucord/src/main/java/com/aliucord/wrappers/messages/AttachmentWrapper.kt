/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticAttachmentWrapper")
package com.aliucord.wrappers.messages

import com.discord.api.message.attachment.MessageAttachment
import com.discord.api.message.attachment.MessageAttachmentType

val MessageAttachment.filename: String
  get() = a()

val MessageAttachment.height: Int
  get() = b()

val MessageAttachment.proxyUrl: String
  get() = c()

val MessageAttachment.size: Long
  get() = d()

val MessageAttachment.type: MessageAttachmentType
  get() = e()

val MessageAttachment.url: String
  get() = f()

val MessageAttachment.width: Int
  get() = g()

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
    @Deprecated(
      "For java: use StaticAttachmentWrapper.getFilename\nFor kotlin: use attachment.filename",
      ReplaceWith("attachment.filename"),
      DeprecationLevel.ERROR,
    )
    fun getFilename(attachment: MessageAttachment) = attachment.filename

    @JvmStatic
    @Deprecated(
      "For java: use StaticAttachmentWrapper.getHeight\nFor kotlin: use attachment.height",
      ReplaceWith("attachment.height"),
      DeprecationLevel.ERROR,
    )
    fun getHeight(attachment: MessageAttachment) = attachment.height

    @JvmStatic
    @Deprecated(
      "For java: use StaticAttachmentWrapper.getProxyUrl\nFor kotlin: use attachment.proxyUrl",
      ReplaceWith("attachment.proxyUrl"),
      DeprecationLevel.ERROR,
    )
    fun getProxyUrl(attachment: MessageAttachment) = attachment.proxyUrl

    @JvmStatic
    @Deprecated(
      "For java: use StaticAttachmentWrapper.getSize\nFor kotlin: use attachment.size",
      ReplaceWith("attachment.size"),
      DeprecationLevel.ERROR,
    )
    fun getSize(attachment: MessageAttachment) = attachment.size

    @JvmStatic
    @Deprecated(
      "For java: use StaticAttachmentWrapper.getType\nFor kotlin: use attachment.type",
      ReplaceWith("attachment.type"),
      DeprecationLevel.ERROR,
    )
    fun getType(attachment: MessageAttachment) = attachment.type

    @JvmStatic
    @Deprecated(
      "For java: use StaticAttachmentWrapper.getUrl\nFor kotlin: use attachment.url",
      ReplaceWith("attachment.url"),
      DeprecationLevel.ERROR,
    )
    fun getUrl(attachment: MessageAttachment) = attachment.url

    @JvmStatic
    @Deprecated(
      "For java: use StaticAttachmentWrapper.getWidth\nFor kotlin: use attachment.width",
      ReplaceWith("attachment.width"),
      DeprecationLevel.ERROR,
    )
    fun getWidth(attachment: MessageAttachment) = attachment.width
  }
}
