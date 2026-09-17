package com.aliucord.coreplugins.voice.model

import com.aliucord.coreplugins.voice.codecCaps
import com.aliucord.utils.SerializedName
import com.discord.rtcconnection.socket.io.Payloads

internal data class NewCodecInfo(
    val name: String,
    val type: String,
    val priority: Int,
    @SerializedName("payload_type") val payloadType: Int,
    @SerializedName("rtx_payload_type") val rtxPayloadType: Int?,
    val encode: Boolean?,
    val decode: Boolean?,
    ) {
        companion object {
            fun from(old: Payloads.Protocol.CodecInfo): NewCodecInfo = with(old) {
                val capability = if (type == "audio") null else codecCaps(name)

                NewCodecInfo(
                    name = name,
                    type = type,
                    priority = priority,
                    payloadType = payloadType,
                    rtxPayloadType = if (capability != null) rtxPayloadType else null,
                    encode = capability?.encode,
                    decode = capability?.decode,
                )
            }
        }
    }
