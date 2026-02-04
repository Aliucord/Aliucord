package com.discord.api.message.poll

data class MessagePollAnswerCount(
    var id: Int,
    var count: Int,
    var meVoted: Boolean,
)
