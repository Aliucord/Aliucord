package com.discord.native.engine

data class VideoInputDeviceDescription(
    val name: String,
    val guid: String,
    val facing: VideoInputDeviceFacing,
)
