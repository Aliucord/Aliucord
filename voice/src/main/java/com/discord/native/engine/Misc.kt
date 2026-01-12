package com.discord.native.engine

data class AudioInputDeviceDescription(
    val name: String,
    val guid: String,
)
data class AudioInputInitializationInfo(
    val description: AudioInputDeviceDescription,
    val timeToInitializedNanos: Long,
)

data class AudioOutputDeviceDescription(
    val name: String,
    val guid: String,
)

data class ConnectionInfo @JvmOverloads constructor(
    val isConnected: Boolean,
    val protocol: String,
    val localAddress: String,
    val localPort: Int,
    val createConnectionTime: Int? = null,
    val connectTime: Int? = null,
)

enum class VideoInputDeviceFacing {
    Unknown,
    Front,
    Back,
}

data class VideoInputDeviceDescription(
    val name: String,
    val guid: String,
    val facing: VideoInputDeviceFacing,
)
