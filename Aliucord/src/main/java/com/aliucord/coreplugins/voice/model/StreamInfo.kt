package com.aliucord.coreplugins.voice.model

// One overlay refresh worth of stream stats. Rows are hidden then the value is null/missing/empty
internal class StreamInfo(
    val codec: String,
    val encoder: String?,
    val decoder: String?,
    val resolution: String,
    val fps: String,
    val bitrate: String,
    val decodeTime: String,
    val decodeFps: String?,
    val packetsLost: String?,
    val freezes: String?,
    val recovery: String?,
)
