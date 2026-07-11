package com.aliucord.coreplugins.voice

import com.aliucord.utils.SerializedName
import com.discord.rtcconnection.socket.io.Payloads
import com.hammerandchisel.libdiscord.Discord

private fun codecCaps(codec: String): Discord.CodecCapability =
    Discord.codecCapabilities[codec] ?: (codec == "H264").let {
        Discord.CodecCapability(codec, decode = it, encode = it)
    }

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

    companion object {
        fun from(old: Payloads.Protocol): NewSelectProtocolPayload {
            return with(old) {
                val secureData = data
                    .takeUnless { it.mode.startsWith("aead_") }
                    ?: Payloads.Protocol.ProtocolInfo(data.address, data.port, VoiceChatFixSettings.MODE_AES256_GCM)

                NewSelectProtocolPayload(
                    codecs = codecs.map { NewCodecInfo.from(it) },
                    data = secureData,
                    protocol = protocol,
                )
            }
        }
    }
}
