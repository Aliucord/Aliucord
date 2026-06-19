package com.aliucord.coreplugins.voice

import com.aliucord.utils.SerializedName
import com.discord.rtcconnection.socket.io.Payloads

data class NewSelectProtocolPayload(
    val codecs: List<NewCodecInfo>,
    val data: Payloads.Protocol.ProtocolInfo,
    val protocol: String,
) {
    data class NewCodecInfo(
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
                if (old.type == "audio") {
                    return with(old) {
                        NewCodecInfo(
                            name = name,
                            type = type,
                            priority = priority,
                            payloadType = payloadType,
                            rtxPayloadType = null,
                            encode = null,
                            decode = null,
                        )
                    }
                } else {
                    return with(old) {
                        NewCodecInfo(
                            name = name,
                            type = type,
                            priority = priority,
                            payloadType = payloadType,
                            rtxPayloadType = rtxPayloadType,
                            encode = true,
                            decode = true,
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun from(old: Payloads.Protocol): NewSelectProtocolPayload {
            return with(old) {
                val secureData =
                    if (data.mode.startsWith("aead_")) data
                    else Payloads.Protocol.ProtocolInfo(data.address, data.port, VoiceChatFixSettings.MODE_AES256_GCM)
                NewSelectProtocolPayload(
                    codecs = codecs.map { NewCodecInfo.from(it) },
                    data = secureData,
                    protocol = protocol,
                )
            }
        }
    }
}
