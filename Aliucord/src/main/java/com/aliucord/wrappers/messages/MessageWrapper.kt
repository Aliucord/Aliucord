package com.aliucord.wrappers.messages

import com.discord.api.message.poll.MessagePoll
import java.lang.reflect.Field
import com.discord.api.message.Message as ApiMessage
import com.discord.models.message.Message as ModelMessage

private val apiPollField: Field = ApiMessage::class.java.getDeclaredField("poll")
private val modelPollField: Field = ModelMessage::class.java.getDeclaredField("poll")

var ApiMessage.poll
    get() = apiPollField[this] as MessagePoll?
    set(it) = apiPollField.set(this, it)

var ModelMessage.poll
    get() = modelPollField[this] as MessagePoll?
    set(it) = modelPollField.set(this, it)
