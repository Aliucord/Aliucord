package com.aliucord.coreplugins.voice.model

import com.aliucord.utils.SerializedName

// GatewayEvent: VOICE_CHANNEL_START_TIME_UPDATE
// voice_start_time is a Unix timestamp in SECONDS (official discord gateway docs)
internal data class VoiceChannelStartTime(
    // The channel arrives as "id", not "channel_id" (ragebait)
    @SerializedName("id") val id: Long?,
    @SerializedName("guild_id") val guildId: Long?,
    @SerializedName("voice_start_time") val voiceStartTime: Long?,
)

// GatewayEvent: VOICE_CHANNEL_STATUS_UPDATE
internal data class VoiceChannelStatus(
    @SerializedName("id") val id: Long?,
    @SerializedName("guild_id") val guildId: Long?,
    @SerializedName("status") val status: String?,
)
