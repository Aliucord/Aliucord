package com.aliucord.coreplugins.voice.model

// One overlay refresh worth of stream stats. encoder/decoder are null when absent
internal class StreamInfo(
    val codec: String,
    val encoder: String?,
    val decoder: String?,
    val resolution: String,
    val fps: String,
    val bitrate: String,
)
