package com.aliucord.coreplugins.voice.model

// Voice transport encryption modes
// Starting November 18th 2024, the voice gateway will only accept the modes
// that use rtpsize framing
internal object TransportModes {
    // Preferred transport mode, only offered when hardware supports it
    const val AES256_GCM = "aead_aes256_gcm_rtpsize"

    // Transport mode always offered by the voice gateway as fallback
    const val XCHACHA20 = "aead_xchacha20_poly1305_rtpsize"

    // Deprecated, used by the ProtocolInfo hook as the replacement mode when the
    // Aliuvoice lib is missing (old libdiscord only speaks the xsalsa* variants)
    const val XSALSA20_LITE = "xsalsa20_poly1305_lite_rtpsize"

    // Deprecated, mode that base aliucord hardcodes in Payloads.Protocol
    const val XSALSA20 = "xsalsa20_poly1305"

    // Some other transport modes below:
    // aead_aes256_gcm - deprecated, AES-GCM variant without rtpsize framing
    // xsalsa20_poly1305_suffix - deprecated, random 24-byte nonce variant
    // xsalsa20_poly1305_lite - deprecated, incremental nonce variant without rtpsize framing
}
