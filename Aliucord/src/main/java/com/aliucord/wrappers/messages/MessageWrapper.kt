package com.aliucord.wrappers.messages

import com.discord.api.message.poll.MessagePoll
import java.lang.reflect.Field
import com.discord.api.message.Message as ApiMessage
import com.discord.models.message.Message as ModelMessage

/**
 * Wraps message classes to add new fields from smali patches
 */
class MessageWrapper {
    companion object {
        private val apiPollField: Field = ApiMessage::class.java.getDeclaredField("poll")
        private val modelPollField: Field = ModelMessage::class.java.getDeclaredField("poll")

        @JvmStatic
        var ApiMessage.poll: MessagePoll?
            get() = apiPollField.get(this) as MessagePoll?
            set(it) = apiPollField.set(this, it)

        @JvmStatic
        var ModelMessage.poll: MessagePoll?
            get() = modelPollField.get(this) as MessagePoll?
            set(it) = modelPollField.set(this, it)
    }
}
