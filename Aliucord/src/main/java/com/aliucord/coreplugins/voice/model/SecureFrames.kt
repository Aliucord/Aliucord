package com.aliucord.coreplugins.voice.model

internal data class SecureFrames(
    val epochAuthenticator: String,
    val version: Int,
)
