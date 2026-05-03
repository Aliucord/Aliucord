package com.discord.api.botuikit

data class UnfurledMediaItem(
    val url: String,
    val proxyUrl: String,
    val height: Int,
    val width: Int,
    val contentType: String?,
    val attachmentId: Long?,
)
