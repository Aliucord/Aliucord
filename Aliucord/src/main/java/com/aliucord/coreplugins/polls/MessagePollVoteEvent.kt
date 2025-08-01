package com.aliucord.coreplugins.polls

import com.aliucord.utils.SerializedName

/** Structure of a gateway poll vote update. */
data class MessagePollVoteEvent(
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("channel_id")
    val channelId: Long,
    @SerializedName("message_id")
    val messageId: Long,
    @SerializedName("guild_id")
    val guildId: Long,
    @SerializedName("answer_id")
    val answerId: Int,
)
