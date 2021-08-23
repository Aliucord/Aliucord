package com.aliucord.api.kotlin

import com.aliucord.api.CommandsAPI
import com.aliucord.api.kotlin.builder.CommandResultBuilder
import com.aliucord.api.kotlin.builder.EmbedBuilder
import com.aliucord.entities.MessageEmbedBuilder

inline fun embedBuilder(
    builder: EmbedBuilder.() -> Unit
) = with(EmbedBuilder().apply(builder)) {
    MessageEmbedBuilder()
        .setAuthor(author?.name, author?.iconUrl, author?.proxyIconUrl ?: author?.iconUrl)
        .setColor(color?.toInt())
        .setDescription(description)
        .setFields(fields.map { MessageEmbedBuilder.createField(it.name, it.value, it.inline) })
        .setFooter(footer?.text, footer?.iconUrl, footer?.proxyIconUrl ?: footer?.iconUrl)
        .setImage(image?.imageUrl, image?.proxyImageUrl ?: image?.imageUrl, image?.imageHeight, image?.imageWidth)
        .setThumbnail(thumbnail?.imageUrl, thumbnail?.proxyImageUrl ?: thumbnail?.imageUrl, thumbnail?.imageHeight, thumbnail?.imageWidth)
        .setTimestamp(timestamp)
        .setTitle(title)
        .setUrl(url)
        .setVideo(video?.videoUrl, video?.proxyVideoUrl ?: video?.videoUrl, video?.videoHeight, video?.videoWidth)
        .apply {
            if (type != null) {
                setType(type)
            }

            if (provider != null) {
                setProvider(provider)
            }
        }
        .build()
}!!

inline fun commandResult(
    builder: CommandResultBuilder.() -> Unit
) = with (CommandResultBuilder().apply(builder)) {
    CommandsAPI.CommandResult(
        content,
        embeds,
        send,
        clyde?.clydeUsername,
        clyde?.clydeAvatarUrl
    )
}