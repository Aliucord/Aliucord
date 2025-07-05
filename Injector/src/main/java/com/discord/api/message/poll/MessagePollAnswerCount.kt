package com.discord.api.message.poll

data class MessagePollAnswerCount(
    var id: Int,
    var count: Int,
    @b.i.d.p.b("me_voted")
    var meVoted: Boolean,
)
