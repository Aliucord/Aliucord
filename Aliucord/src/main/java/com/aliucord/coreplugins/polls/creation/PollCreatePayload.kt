package com.aliucord.coreplugins.polls.creation

import com.aliucord.entities.RNMessage
import com.discord.api.message.poll.MessagePoll

@Suppress("unused")
internal class PollCreatePayload(private val poll: MessagePoll) : RNMessage()
