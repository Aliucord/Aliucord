package com.discord.api.message.poll

import com.discord.api.message.reaction.MessageReactionEmoji

data class MessagePollMedia(
    var text: String,
    var emoji: MessageReactionEmoji?,
)
