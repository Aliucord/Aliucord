package com.aliucord.coreplugins.voice.model

import com.discord.rtcconnection.socket.io.Payloads

internal data class NewSelectProtocolPayload(
    val codecs: List<NewCodecInfo>,
    val data: Payloads.Protocol.ProtocolInfo,
    val protocol: String,
) {
    companion object {
        fun from(old: Payloads.Protocol): NewSelectProtocolPayload {
            return with(old) {
                val secureData = data
                    .takeUnless { it.mode.startsWith("aead_") }
                    ?: Payloads.Protocol.ProtocolInfo(data.address, data.port, TransportModes.AES256_GCM)

                NewSelectProtocolPayload(
                    codecs = codecs.map { NewCodecInfo.from(it) },
                    data = secureData,
                    protocol = protocol,
                )
            }
        }
    }
}
