package com.aliucord.voice

/**
 * Voice fork ABI version. Read via reflection by the VoiceChatFix core plugin's version gate
 * (EXPECTED_LIB_VERSION in the voice module). Bump together with the bundled
 * libdiscord.so / webrtc dex pairing.
 */
object BuildConfig {
    const val VERSION = "90.0.19-codec-api.b2"
    const val LIBDISCORD_BASE = "333.5"
}
