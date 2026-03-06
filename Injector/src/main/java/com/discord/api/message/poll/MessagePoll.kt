package com.discord.api.message.poll

import com.discord.api.utcdatetime.UtcDateTime

data class MessagePoll(
    val question: MessagePollMedia,
    val answers: List<MessagePollAnswer>,
    val results: MessagePollResult?,
    val expiry: UtcDateTime?,
    val duration: Int?,
    val allowMultiselect: Boolean,
    val layoutType: Int,
)
