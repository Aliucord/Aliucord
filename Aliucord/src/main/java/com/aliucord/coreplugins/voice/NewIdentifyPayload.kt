package com.aliucord.coreplugins.voice

import com.aliucord.utils.SerializedName

data class NewIdentifyPayload(
    @SerializedName("server_id") val serverId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("session_id") val sessionId: String,
    val token: String,
    @SerializedName("max_dave_protocol_version") val maxDaveProtocolVersion: Int,
)
