package com.discord.api.message.poll

data class MessagePollResult(
    @b.i.d.p.b("is_finalized")
    val isFinalized: Boolean,
    @b.i.d.p.b("answer_counts")
    val answerCounts: List<MessagePollAnswerCount>,
)
