package com.aliucord.coreplugins.voice.model

internal enum class VoiceCloseCodes(
    val code: Int,
    val message: String,
    val toast: Boolean = true,
) {
    UNKNOWN_OPCODE(4001, "Unknown opcode sent to voice server"),
    FAILED_TO_DECODE_PAYLOAD(4002, "Voice server failed to decode payload"),
    NOT_AUTHENTICATED(4003, "Not authenticated with voice server"),
    AUTHENTICATION_FAILED(4004, "Voice authentication failed"),
    ALREADY_AUTHENTICATED(4005, "Already authenticated with voice server"),
    SESSION_NO_LONGER_VALID(4006, "Voice session no longer valid"),
    SESSION_TIMEOUT(4009, "Voice session timed out"),
    SERVER_NOT_FOUND(4011, "Voice server not found"),
    UNKNOWN_PROTOCOL(4012, "Unknown voice protocol"),
    WEBRTC_CRASHED(4013, "WebRTC connection crashed, resuming"),
    DISCONNECTED(4014, "Disconnected by server", false),
    VOICE_SERVER_CRASHED(4015, "Voice server crashed, resuming"),
    UNKNOWN_ENCRYPTION_MODE(4016, "Unknown transport encryption mode"),
    DAVE_REQUIRED(4017, "E2EE/DAVE protocol required", false),  // handled by patchDaveEnforcement
    BAD_REQUEST(4020, "Bad request sent to voice server"),
    RATE_LIMITED(4021, "Rate limited by voice server"),
    CALL_TERMINATED(4022, "Call terminated", false);

    fun friendly() = "$code ($name)"

    companion object {
        private val lookup = entries.associateBy { it.code }

        fun from(code: Int): VoiceCloseCodes? = lookup[code]
    }
}
