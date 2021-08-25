/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.*
import com.discord.api.utcdatetime.UtcDateTime

/**
 * Wraps the obfuscated [MessageEmbed] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class MessageEmbedWrapper(private val embed: MessageEmbed) {
  /** Returns the raw (obfuscated) [MessageEmbed] Object associated with this wrapper */
  fun raw() = embed

  val author
    get() = getAuthor(embed)

  /** Returns the raw (obfuscated) [EmbedAuthor] Object associated with this wrapper */
  val rawAuthor
    get() = embed.rawAuthor

  val color
    get() = embed.color

  val description
    get() = embed.description

  val fields
    get() = getFields(embed)

  /** Returns the raw (obfuscated) [EmbedField]s associated with this wrapper */
  val rawFields
    get() = embed.rawFields

  val footer
    get() = getFooter(embed)

  /** Returns the raw (obfuscated) [EmbedFooter] Object associated with this wrapper */
  val rawFooter
    get() = embed.rawFooter

  val thumbnail
    get() = getThumbnail(embed)

  /** Returns the raw (obfuscated) [EmbedThumbnail] Object associated with this wrapper */
  val rawThumbnail
    get() = embed.rawThumbnail

  val image
    get() = getImage(embed)

  /** Returns the raw (obfuscated) [EmbedImage] Object associated with this wrapper */
  val rawImage
    get() = embed.rawImage

  val video
    get() = getVideo(embed)

  /** Returns the raw (obfuscated) [EmbedVideo] Object associated with this wrapper */
  val rawVideo
    get() = embed.rawVideo

  val provider
    get() = getProvider(embed)

  /** Returns the raw (obfuscated) [EmbedProvider] Object associated with this wrapper */
  val rawProvider
    get() = embed.rawProvider

  val timestamp
    get() = embed.timestamp

  val title
    get() = embed.title

  val type
    get() = embed.type

  val url
    get() = embed.url

  companion object {
    @JvmStatic
    fun getAuthor(embed: MessageEmbed) = embed.rawAuthor
      .run { if (this == null) null else AuthorWrapper(this) }

    @JvmStatic
    fun getFields(embed: MessageEmbed) = embed.rawFields.map { FieldWrapper(it) }

    @JvmStatic
    fun getFooter(embed: MessageEmbed) = embed.rawFooter
      .run { if (this == null) null else FooterWrapper(this) }

    @JvmStatic
    fun getImage(embed: MessageEmbed) = embed.rawImage
      .run { if (this == null) null else ImageWrapper(this) }

    @JvmStatic
    fun getProvider(embed: MessageEmbed) = embed.rawProvider
      .run { if (this == null) null else ProviderWrapper(this) }

    @JvmStatic
    fun getThumbnail(embed: MessageEmbed) = embed.rawThumbnail
      .run { if (this == null) null else ThumbnailWrapper(this) }

    @JvmStatic
    fun getVideo(embed: MessageEmbed) = embed.rawVideo
      .run { if (this == null) null else VideoWrapper(this) }

    @JvmStatic
    val MessageEmbed.rawAuthor: EmbedAuthor?
      get() = a()

    @JvmStatic
    val MessageEmbed.color: Int?
      get() = b()

    @JvmStatic
    val MessageEmbed.description: String?
      get() = c()

    @JvmStatic
    val MessageEmbed.rawFields: List<EmbedField>
      get() = d()

    @JvmStatic
    val MessageEmbed.rawFooter: EmbedFooter?
      get() = e()

    @JvmStatic
    val MessageEmbed.rawThumbnail: EmbedThumbnail?
      get() = h()

    @JvmStatic
    val MessageEmbed.rawImage: EmbedImage?
      get() = f()

    @JvmStatic
    val MessageEmbed.rawVideo: EmbedVideo?
      get() = m()

    @JvmStatic
    val MessageEmbed.rawProvider: EmbedProvider?
      get() = g()

    @JvmStatic
    val MessageEmbed.timestamp: UtcDateTime?
      get() = i()

    @JvmStatic
    val MessageEmbed.title: String?
      get() = j()

    @JvmStatic
    val MessageEmbed.type: EmbedType
      get() = k()

    @JvmStatic
    val MessageEmbed.url: String?
      get() = l()
  }
}
