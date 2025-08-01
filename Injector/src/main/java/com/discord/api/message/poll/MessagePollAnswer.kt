package com.discord.api.message.poll

data class MessagePollAnswer(
    @b.i.d.p.b("answer_id")
    val answerId: Int?,
    @b.i.d.p.b("poll_media")
    val pollMedia: MessagePollMedia,
)
