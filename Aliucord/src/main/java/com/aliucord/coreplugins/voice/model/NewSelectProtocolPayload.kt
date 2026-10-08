package com.aliucord.coreplugins.voice.model

import com.discord.rtcconnection.socket.io.Payloads

internal data class NewSelectProtocolPayload(
    val codecs: List<NewCodecInfo>,
    val data: Payloads.Protocol.ProtocolInfo,
    val protocol: String,
) {
    companion object {
        fun from(old: Payloads.Protocol): NewSelectProtocolPayload {
            val secureData = old.data
                .takeUnless { it.mode.startsWith("aead_") }
                ?: Payloads.Protocol.ProtocolInfo(
                    old.data.address,
                    old.data.port,
                    TransportModes.AES256_GCM,
                )

            return NewSelectProtocolPayload(
                codecs = old.codecs.map(NewCodecInfo::from),
                data = secureData,
                protocol = old.protocol,
            )
        }
    }
}
