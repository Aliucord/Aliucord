/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticMessageEmbedWrapper")
package com.aliucord.wrappers.embeds

import com.discord.api.message.embed.*
import com.discord.api.utcdatetime.UtcDateTime

val MessageEmbed.author: EmbedAuthor?
  get() = a()

val MessageEmbed.color: Int?
  get() = b()

val MessageEmbed.description: String?
  get() = c()

val MessageEmbed.fields: List<EmbedField>
  get() = d()

val MessageEmbed.footer: EmbedFooter?
  get() = e()

val MessageEmbed.thumbnail: EmbedThumbnail?
  get() = h()

val MessageEmbed.image: EmbedImage?
  get() = f()

val MessageEmbed.video: EmbedVideo?
  get() = m()

val MessageEmbed.provider: EmbedProvider?
  get() = g()

val MessageEmbed.timestamp: UtcDateTime?
  get() = i()

val MessageEmbed.title: String?
  get() = j()

val MessageEmbed.type: EmbedType
  get() = k()

val MessageEmbed.url: String?
  get() = l()

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
    get() = embed.author

  val color
    get() = embed.color

  val description
    get() = embed.description

  val fields
    get() = getFields(embed)

  /** Returns the raw (obfuscated) [EmbedField]s associated with this wrapper */
  val rawFields
    get() = embed.fields

  val footer
    get() = getFooter(embed)

  /** Returns the raw (obfuscated) [EmbedFooter] Object associated with this wrapper */
  val rawFooter
    get() = embed.footer

  val thumbnail
    get() = getThumbnail(embed)

  /** Returns the raw (obfuscated) [EmbedThumbnail] Object associated with this wrapper */
  val rawThumbnail
    get() = embed.thumbnail

  val image
    get() = getImage(embed)

  /** Returns the raw (obfuscated) [EmbedImage] Object associated with this wrapper */
  val rawImage
    get() = embed.image

  val video
    get() = getVideo(embed)

  /** Returns the raw (obfuscated) [EmbedVideo] Object associated with this wrapper */
  val rawVideo
    get() = embed.video

  val provider
    get() = getProvider(embed)

  /** Returns the raw (obfuscated) [EmbedProvider] Object associated with this wrapper */
  val rawProvider
    get() = embed.provider

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
    fun getAuthor(embed: MessageEmbed) = embed.author
      .run { if (this == null) null else AuthorWrapper(this) }

    @JvmStatic
    fun getFields(embed: MessageEmbed) = embed.fields.map { FieldWrapper(it) }

    @JvmStatic
    fun getFooter(embed: MessageEmbed) = embed.footer
      .run { if (this == null) null else FooterWrapper(this) }

    @JvmStatic
    fun getImage(embed: MessageEmbed) = embed.image
      .run { if (this == null) null else ImageWrapper(this) }

    @JvmStatic
    fun getProvider(embed: MessageEmbed) = embed.provider
      .run { if (this == null) null else ProviderWrapper(this) }

    @JvmStatic
    fun getThumbnail(embed: MessageEmbed) = embed.thumbnail
      .run { if (this == null) null else ThumbnailWrapper(this) }

    @JvmStatic
    fun getVideo(embed: MessageEmbed) = embed.video
      .run { if (this == null) null else VideoWrapper(this) }

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getRawAuthor\nFor kotlin: use embed.author",
      ReplaceWith("embed.author"),
      DeprecationLevel.ERROR,
    )
    fun getRawAuthor(embed: MessageEmbed) = embed.author

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getColor\nFor kotlin: use embed.color",
      ReplaceWith("embed.color"),
      DeprecationLevel.ERROR,
    )
    fun getColor(embed: MessageEmbed) = embed.color

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getDescription\nFor kotlin: use embed.description",
      ReplaceWith("embed.description"),
      DeprecationLevel.ERROR,
    )
    fun getDescription(embed: MessageEmbed) = embed.description

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getRawFields\nFor kotlin: use embed.fields",
      ReplaceWith("embed.author"),
      DeprecationLevel.ERROR,
    )
    fun getRawFields(embed: MessageEmbed) = embed.fields

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getRawFooter\nFor kotlin: use embed.footer",
      ReplaceWith("embed.footer"),
      DeprecationLevel.ERROR,
    )
    fun getRawFooter(embed: MessageEmbed) = embed.footer

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getRawThumbnail\nFor kotlin: use embed.thumbnail",
      ReplaceWith("embed.thumbnail"),
      DeprecationLevel.ERROR,
    )
    fun getRawThumbnail(embed: MessageEmbed) = embed.thumbnail

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getRawImage\nFor kotlin: use embed.image",
      ReplaceWith("embed.image"),
      DeprecationLevel.ERROR,
    )
    fun getRawImage(embed: MessageEmbed) = embed.image

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getRawProvider\nFor kotlin: use embed.provider",
      ReplaceWith("embed.provider"),
      DeprecationLevel.ERROR,
    )
    fun getRawProvider(embed: MessageEmbed) = embed.provider

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getTimestamp\nFor kotlin: use embed.timestamp",
      ReplaceWith("embed.timestamp"),
      DeprecationLevel.ERROR,
    )
    fun getTimestamp(embed: MessageEmbed) = embed.timestamp

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getTitle\nFor kotlin: use embed.title",
      ReplaceWith("embed.title"),
      DeprecationLevel.ERROR,
    )
    fun getTitle(embed: MessageEmbed) = embed.title

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getType\nFor kotlin: use embed.type",
      ReplaceWith("embed.type"),
      DeprecationLevel.ERROR,
    )
    fun getType(embed: MessageEmbed) = embed.type

    @JvmStatic
    @Deprecated(
      "For java: use StaticMessageEmbedWrapper.getUrl\nFor kotlin: use embed.url",
      ReplaceWith("embed.url"),
      DeprecationLevel.ERROR,
    )
    fun getUrl(embed: MessageEmbed) = embed.url
  }
}
