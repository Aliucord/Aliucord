package com.discord.api.botuikit

data class ThumbnailComponent(
    private val type: ComponentType,
    val id: Int,
    val media: UnfurledMediaItem,
    val description: String?,
    val spoiler: Boolean,
) : ContentComponent() {
    override fun getType() = type
}
