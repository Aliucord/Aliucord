package com.discord.api.message.poll

import com.discord.api.utcdatetime.UtcDateTime

data class MessagePoll(
    val question: MessagePollMedia,
    val answers: List<MessagePollAnswer>,
    val results: MessagePollResult?,
    val expiry: UtcDateTime?,
    val duration: Int?,
    @b.i.d.p.b("allow_multiselect")
    val allowMultiselect: Boolean,
    @b.i.d.p.b("layout_type")
    val layoutType: Int,
)
