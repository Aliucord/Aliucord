package com.aliucord.coreplugins.voice.model

import com.aliucord.utils.SerializedName

// Sent as Opcode 43 REQUEST_CHANNEL_INFO, answered with a CHANNEL_INFO dispatch
internal data class ChannelInfoRequest(
    @SerializedName("guild_id") val guildId: Long,
    @SerializedName("fields") val fields: List<String>,
)

// GatewayEvent: CHANNEL_INFO
internal data class ChannelInfo(
    @SerializedName("guild_id") val guildId: Long?,
    @SerializedName("channels") val channels: List<Entry>?,
) {
    internal data class Entry(
        @SerializedName("id") val id: Long?,
        @SerializedName("status") val status: String?,
        // Unix seconds, same as VOICE_CHANNEL_START_TIME_UPDATE
        @SerializedName("voice_start_time") val voiceStartTime: Long?,
    )
}
