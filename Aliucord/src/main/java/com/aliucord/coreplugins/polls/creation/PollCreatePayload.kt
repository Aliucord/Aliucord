package com.aliucord.coreplugins.polls.creation

import com.aliucord.Utils
import com.discord.api.message.poll.MessagePoll
import kotlin.random.Random

@Suppress("unused", "PrivatePropertyName")
internal data class PollCreatePayload(private val poll: MessagePoll) {
    private val mobile_network_type = "wifi"
    private val signal_strength = Random.nextInt(1, 4) // TODO: Use real values maybe?
    private val content = ""
    private val nonce = Utils.generateRNNonce()
    private val tts = false
    private val flags = 0
}
