package com.aliucord.api.kotlin.builder

import com.aliucord.api.kotlin.embedBuilder
import com.discord.api.message.embed.MessageEmbed

class CommandResultBuilder {

    /**
     * The message content
     */
    var content: String? = null

    /**
     * Whether the result should be sent visible for everyone
     */
    var send: Boolean = true

    /**
     * Pseudo clyde associated with this CommandResult
     */
    var clyde: Clyde? = null

    /**
     *  The embeds
     */
    val embeds = mutableListOf<MessageEmbed>()

    fun addEmbed(builder: EmbedBuilder.() -> Unit) {
        embeds.add(embedBuilder(builder))
    }

    fun clyde(builder: Clyde.() -> Unit) {
        Clyde().apply(builder)
    }

    class Clyde {

        /**
         * The username of the pseudo clyde associated with this CommandResult
         */
        var clydeUsername: String? = null

        /**
         * The avatar url of the pseudo clyde associated with this CommandResult
         */
        var clydeAvatarUrl: String? = null
    }



}