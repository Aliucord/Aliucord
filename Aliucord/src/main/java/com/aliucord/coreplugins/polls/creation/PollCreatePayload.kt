package com.aliucord.coreplugins.polls.creation

import com.discord.api.message.poll.MessagePoll
import com.discord.utilities.SnowflakeUtils
import kotlin.random.Random

@Suppress("unused", "PrivatePropertyName")
internal data class PollCreatePayload(private val poll: MessagePoll) {
    private val mobile_network_type = "wifi"
    private val signal_strength = Random.nextInt(1, 4) // TODO: Use real values maybe?
    private val content = ""
    // For nonce, there is NonceGenerator, but it seems to use time in the future. RN and Desktop doesn't do this,
    // so it also wasn't used here. Instead we just generate a random long and add it with current time
    private val nonce = SnowflakeUtils.fromTimestamp(System.currentTimeMillis()) + Random.nextBits(23)
    private val tts = false
    private val flags = 0
}
