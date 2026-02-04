package com.discord.api.message.poll

data class MessagePollResult(
    val isFinalized: Boolean,
    val answerCounts: List<MessagePollAnswerCount>,
)
