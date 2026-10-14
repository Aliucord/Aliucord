package com.aliucord.coreplugins.voice.model

import com.aliucord.utils.SerializedName

internal data class VoiceChannelEffect(
    @SerializedName("channel_id") val channelId: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("sound_id") val soundId: String?,
    @SerializedName("sound_volume") val soundVolume: Float?,
    val emoji: Emoji?,
) {
    internal data class Emoji(
        val id: String?,
        val name: String?,
    )
}
