package com.aliucord.coreplugins.voice.model

import com.aliucord.utils.SerializedName
import com.discord.rtcconnection.socket.io.Payloads
import com.hammerandchisel.libdiscord.Discord

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
        fun from(old: Payloads.Protocol.CodecInfo): NewCodecInfo {
            val capability = when (old.type) {
                "audio" -> null

                else -> Discord.codecCapabilities[old.name] ?: Discord.CodecCapability(
                    codec = old.name,
                    decode = old.name == "H264",
                    encode = old.name == "H264",
                )
            }

            return NewCodecInfo(
                name = old.name,
                type = old.type,
                priority = old.priority,
                payloadType = old.payloadType,
                rtxPayloadType = capability?.let { old.rtxPayloadType },
                encode = capability?.encode,
                decode = capability?.decode,
            )
        }
    }
}
