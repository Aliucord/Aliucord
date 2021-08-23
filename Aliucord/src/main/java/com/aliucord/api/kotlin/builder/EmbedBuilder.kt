package com.aliucord.api.kotlin.builder

import com.discord.api.message.embed.EmbedProvider
import com.discord.api.message.embed.EmbedType
import com.discord.api.utcdatetime.UtcDateTime

class EmbedBuilder {

    var author: Author? = null
    var color: Long? = null
    var description: String? = null
    var footer: Footer? = null
    var image: Image? = null
    var provider: EmbedProvider? = null
    var thumbnail: Thumbnail? = null
    var title: String? = null
    var type: EmbedType? = null
    var url: String? = null
    var video: Video? = null
    var timestamp: UtcDateTime? = null

    val fields = mutableListOf<Field>()

    /**
     * Adds a field to the embed.
     */
    inline fun addField(builder: Field.() -> Unit) {
        fields.add(Field().apply(builder))
    }

    /**
     * Sets the embed author.
     */
    inline fun author(builder: Author.() -> Unit) {
        author = Author().apply(builder)
    }

    /**
     * Sets the embed footer.
     */
    inline fun footer(builder: Footer.() -> Unit) {
        footer = Footer().apply(builder)
    }

    /**
     * Sets the embed image
     */
    inline fun image(builder: Image.() -> Unit) {
        image = Image().apply(builder)
    }

    /**
     * Sets the embed thumbnail.
     */
    inline fun thumbnail(builder: Thumbnail.() -> Unit) {
        thumbnail = Thumbnail().apply(builder)
    }

    /**
     * Sets the embed video.
     */
    inline fun video(builder: Video.() -> Unit) {
        video = Video().apply(builder)
    }


    class Author {
        var name: String? = null
        var iconUrl: String? = null
        var proxyIconUrl: String? = null
    }

    class Field {
        var name: String? = null
        var value: String? = null
        var inline: Boolean = false
    }

    class Footer {
        var text: String? = null
        var iconUrl: String? = null
        var proxyIconUrl: String? = null
    }

    class Image {
        var imageUrl: String? = null
        var proxyImageUrl: String? = null
        var imageHeight: Int = DEFAULT_IMAGE_SIZE
        var imageWidth: Int = DEFAULT_IMAGE_SIZE
    }

    class Thumbnail {
        var imageUrl: String? = null
        var proxyImageUrl: String? = null
        var imageHeight: Int = DEFAULT_IMAGE_SIZE
        var imageWidth: Int = DEFAULT_IMAGE_SIZE
    }

    class Video {
        var videoUrl: String? = null
        var proxyVideoUrl: String? = null
        var videoHeight: Int = DEFAULT_IMAGE_SIZE
        var videoWidth: Int = DEFAULT_IMAGE_SIZE
    }

    companion object {
        const val DEFAULT_IMAGE_SIZE = 512
    }

}