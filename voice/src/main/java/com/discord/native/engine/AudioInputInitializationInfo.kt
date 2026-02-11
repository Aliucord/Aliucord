package com.discord.native.engine

data class AudioInputInitializationInfo(
    val description: AudioInputDeviceDescription,
    val timeToInitializedNanos: Long,
)
