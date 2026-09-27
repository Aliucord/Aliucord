package com.aliucord.coreplugins.voice.model

import com.aliucord.utils.SerializedName

// V5: {t}
// V8: `d` = {t, seq_ack}
internal data class HeartbeatPayload(
    val t: Long,
    @SerializedName("seq_ack") val seqAck: Int,
)

internal data class ResumePayload(
    val token: String,
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("server_id") val serverId: String,
    @SerializedName("seq_ack") val seqAck: Int,
)
