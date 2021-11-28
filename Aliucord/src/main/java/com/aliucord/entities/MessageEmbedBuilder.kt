/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.entities

import com.aliucord.Main
import com.aliucord.utils.ReflectUtils
import com.discord.api.message.embed.*
import com.discord.api.utcdatetime.UtcDateTime
import java.lang.reflect.Field
import java.util.*

/** [com.discord.api.message.embed.MessageEmbed] builder  */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class MessageEmbedBuilder @JvmOverloads constructor(type: EmbedType = EmbedType.RICH) {
    init {
        setType(type)
    }

    companion object {
        // reflect moment
        private lateinit var authorField: Field
        private lateinit var colorField: Field
        private lateinit var descriptionField: Field
        private lateinit var fieldsField: Field
        private lateinit var footerField: Field
        private lateinit var imageField: Field
        private lateinit var providerField: Field
        private lateinit var thumbnailField: Field
        private lateinit var timestampField: Field
        private lateinit var titleField: Field
        private lateinit var typeField: Field
        private lateinit var urlField: Field
        private lateinit var videoField: Field

        init {
            try {
                val c = MessageEmbed::class.java
                authorField = c.getDeclaredField("author").apply { isAccessible = true }
                colorField = c.getDeclaredField("color").apply { isAccessible = true }
                descriptionField = c.getDeclaredField("description").apply { isAccessible = true }
                fieldsField = c.getDeclaredField("fields").apply { isAccessible = true }
                footerField = c.getDeclaredField("footer").apply { isAccessible = true }
                imageField = c.getDeclaredField("image").apply { isAccessible = true }
                providerField = c.getDeclaredField("provider").apply { isAccessible = true }
                thumbnailField = c.getDeclaredField("thumbnail").apply { isAccessible = true }
                timestampField = c.getDeclaredField("timestamp").apply { isAccessible = true }
                titleField = c.getDeclaredField("title").apply { isAccessible = true }
                typeField = c.getDeclaredField("type").apply { isAccessible = true }
                urlField = c.getDeclaredField("url").apply { isAccessible = true }
                videoField = c.getDeclaredField("video").apply { isAccessible = true }
            } catch (e: Exception) {
                Main.logger.error(e)
            }
        }
    }
    
    /**
     * @param name Field name.
     * @param value Field content.
     * @param inline Whether to inline the field or not.
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.addField
     */
    fun createField(name: String?, value: String?, inline: Boolean?): EmbedField? {
        val c = EmbedField::class.java
        val field = ReflectUtils.allocateInstance(c)
        try {
            ReflectUtils.setField(c, field, "name", name)
            ReflectUtils.setField(c, field, "value", value)
            ReflectUtils.setField(c, field, "inline", inline)
            return field
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return null
    }


    private val embed = ReflectUtils.allocateInstance(MessageEmbed::class.java)

    /** Builds the MessageEmbed  */
    fun build(): MessageEmbed = embed

    /**
     * @param name Name of the author.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setAuthor(name: String) = setAuthor(name, null, null)

    /**
     * @param name Name of the author.
     * @param iconUrl Icon URL of the author.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setAuthor(name: String, iconUrl: String?) = setAuthor(name, iconUrl, iconUrl)

    /**
     * @param name Name of the author.
     * @param proxyIconUrl Proxy icon URL of the author.
     * @param iconUrl Icon URL of the author.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setAuthor(name: String, iconUrl: String?, proxyIconUrl: String?): MessageEmbedBuilder {
        val c = EmbedAuthor::class.java
        val author = ReflectUtils.allocateInstance(c)
        try {
            ReflectUtils.setField(c, author, "name", name)
            ReflectUtils.setField(c, author, "iconUrl", iconUrl)
            ReflectUtils.setField(c, author, "proxyIconUrl", proxyIconUrl)
            setAuthor(author)
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed author.
     * @param author [EmbedAuthor]
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setAuthor(author: EmbedAuthor): MessageEmbedBuilder {
        try {
            authorField[embed] = author
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets a random embed color.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setRandomColor(): MessageEmbedBuilder =
        setColor(Random().nextInt(0xffffff + 1))

    /**
     * Sets the embed color.
     * @param color Embed color.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setColor(color: Int): MessageEmbedBuilder {
        try {
            colorField[embed] = color
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed description.
     * @param description Embed description.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setDescription(description: String): MessageEmbedBuilder {
        try {
            descriptionField[embed] = description
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Adds a field to the embed.
     * @param name Name of the field.
     * @param value Content of the field.
     * @param inline Whether to inline the field or not.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun addField(name: String, value: String?, inline: Boolean) =
        addField(createField(name, value, inline))

    /**
     * Adds a field to the embed.
     * @param field [EmbedField]
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.addField
     * @see MessageEmbedBuilder.createField
     */
    @Suppress("UNCHECKED_CAST")
    fun addField(field: EmbedField?): MessageEmbedBuilder {
        if (field == null) return this
        try {
            val o = fieldsField[embed] as List<EmbedField>
            val aList = if (o is ArrayList<*>) o as ArrayList<EmbedField> else ArrayList(o)
            aList.add(field)
            fieldsField[embed] = aList
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets embed fields.
     * @param fields [List] of [EmbedField]
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.createField
     */
    fun setFields(fields: List<EmbedField?>): MessageEmbedBuilder {
        try {
            fieldsField[embed] = fields
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed footer.
     * @param text Footer text.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setFooter(text: String?): MessageEmbedBuilder =
        setFooter(text, null, null)

    /**
     * Sets the embed footer.
     * @param text Footer text.
     * @param iconUrl Footer icon URL.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setFooter(text: String?, iconUrl: String?): MessageEmbedBuilder =
        setFooter(text, iconUrl, iconUrl)

    /**
     * Sets the embed footer.
     * @param text Footer text.
     * @param iconUrl Footer icon URL.
     * @param proxyIconUrl Footer Proxy icon URL.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setFooter(text: String?, iconUrl: String?, proxyIconUrl: String?): MessageEmbedBuilder {
        val c = EmbedFooter::class.java
        val footer = ReflectUtils.allocateInstance(c)
        try {
            ReflectUtils.setField(c, footer, "text", text)
            ReflectUtils.setField(c, footer, "iconUrl", iconUrl)
            ReflectUtils.setField(c, footer, "proxyIconUrl", proxyIconUrl)
            setFooter(footer)
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed footer.
     * @param footer [EmbedFooter]
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.setFooter
     */
    fun setFooter(footer: EmbedFooter?): MessageEmbedBuilder {
        try {
            footerField[embed] = footer
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed image.
     * @param imageUrl Image URL.
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.setImage
     */
    fun setImage(imageUrl: String?): MessageEmbedBuilder =
        setImage(imageUrl, imageUrl, 512, 512)

    /**
     * Sets the embed image.
     * @param imageUrl Image URL.
     * @param proxyImageUrl Proxy image URL.
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.setImage
     */
    fun setImage(imageUrl: String?, proxyImageUrl: String?): MessageEmbedBuilder =
        setImage(imageUrl, proxyImageUrl, 512, 512)

    /**
     * Sets the embed image.
     * @param imageUrl Image URL.
     * @param proxyImageUrl Proxy image URL.
     * @param imageHeight Image height.
     * @param imageWidth Image width.
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.setImage
     */
    fun setImage(
        imageUrl: String?,
        proxyImageUrl: String?,
        imageHeight: Int?,
        imageWidth: Int?
    ): MessageEmbedBuilder {
        val c = EmbedImage::class.java
        val image = ReflectUtils.allocateInstance(c)
        try {
            ReflectUtils.setField(c, image, "url", imageUrl)
            ReflectUtils.setField(c, image, "proxyUrl", proxyImageUrl)
            ReflectUtils.setField(c, image, "height", imageHeight)
            ReflectUtils.setField(c, image, "width", imageWidth)
            setImage(image)
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed image.
     * @param image [EmbedImage]
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.setImage
     */
    fun setImage(image: EmbedImage?): MessageEmbedBuilder {
        try {
            imageField[embed] = image
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed provider.
     * @param provider [EmbedProvider].
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setProvider(provider: EmbedProvider?): MessageEmbedBuilder {
        try {
            providerField[embed] = provider
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed thumbnail.
     * @param imageUrl Image URL.
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.setThumbnail
     */
    fun setThumbnail(imageUrl: String?): MessageEmbedBuilder =
        setThumbnail(imageUrl, imageUrl, 512, 512)

    /**
     * Sets the embed thumbnail.
     * @param imageUrl Image URL.
     * @param proxyImageUrl Proxy image URL.
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.setThumbnail
     */
    fun setThumbnail(imageUrl: String?, proxyImageUrl: String?): MessageEmbedBuilder =
        setThumbnail(imageUrl, proxyImageUrl, 512, 512)

    /**
     * Sets the embed thumbnail.
     * @param imageUrl Image URL.
     * @param proxyImageUrl Proxy image URL.
     * @param imageHeight Image height.
     * @param imageWidth Image width.
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.setThumbnail
     */
    fun setThumbnail(
        imageUrl: String?,
        proxyImageUrl: String?,
        imageHeight: Int?,
        imageWidth: Int?
    ): MessageEmbedBuilder {
        val c = EmbedThumbnail::class.java
        val image = ReflectUtils.allocateInstance(c)
        try {
            ReflectUtils.setField(c, image, "url", imageUrl)
            ReflectUtils.setField(c, image, "proxyUrl", proxyImageUrl)
            ReflectUtils.setField(c, image, "height", imageHeight)
            ReflectUtils.setField(c, image, "width", imageWidth)
            setThumbnail(image)
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed thumbnail.
     * @param image [EmbedThumbnail]
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.setThumbnail
     */
    fun setThumbnail(image: EmbedThumbnail?): MessageEmbedBuilder {
        try {
            thumbnailField[embed] = image
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed timestamp.
     * @param timestamp [UtcDateTime] timestamp.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setTimestamp(timestamp: UtcDateTime?): MessageEmbedBuilder {
        try {
            timestampField[embed] = timestamp
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed title.
     * @param title Embed title.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setTitle(title: String): MessageEmbedBuilder {
        try {
            titleField[embed] = title
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed type.
     * @param type [EmbedType].
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setType(type: EmbedType): MessageEmbedBuilder {
        try {
            typeField[embed] = type
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed URL.
     * @param url Embed URL.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setUrl(url: String?): MessageEmbedBuilder {
        try {
            urlField[embed] = url
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed video.
     * @param videoUrl Video URL.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setVideo(
        videoUrl: String?,
        proxyVideoUrl: String?,
        height: Int?,
        width: Int?
    ): MessageEmbedBuilder {
        val c = EmbedVideo::class.java
        val video = ReflectUtils.allocateInstance(c)
        try {
            ReflectUtils.setField(c, video, "url", videoUrl)
            ReflectUtils.setField(c, video, "proxyUrl", proxyVideoUrl)
            ReflectUtils.setField(c, video, "height", height)
            ReflectUtils.setField(c, video, "width", width)
            setVideo(video)
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }

    /**
     * Sets the embed video.
     * @param videoUrl Video URL.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setVideo(videoUrl: String?): MessageEmbedBuilder =
        setVideo(videoUrl, videoUrl, 512, 512)

    /**
     * Sets the embed video.
     * @param videoUrl Video URL.
     * @return [MessageEmbedBuilder] for chaining.
     */
    fun setVideo(videoUrl: String?, proxyVideoUrl: String?): MessageEmbedBuilder =
        setVideo(videoUrl, proxyVideoUrl, 512, 512)

    /**
     * Sets the embed video.
     * @param video [EmbedVideo].
     * @return [MessageEmbedBuilder] for chaining.
     * @see MessageEmbedBuilder.setVideo
     */
    fun setVideo(video: EmbedVideo?): MessageEmbedBuilder {
        try {
            videoField[embed] = video
        } catch (e: Throwable) {
            Main.logger.error(e)
        }
        return this
    }
}
