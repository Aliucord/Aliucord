package com.aliucord.coreplugins.voice

import com.aliucord.utils.SerializedName
import com.discord.rtcconnection.socket.io.Payloads

data class NewIdentifyPayload(
    @SerializedName("server_id") val serverId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("session_id") val sessionId: String,
    val token: String,
    val video: Boolean,
    val streams: List<Payloads.Stream>,
    @SerializedName("max_dave_protocol_version") val maxDaveProtocolVersion: Int,
)
