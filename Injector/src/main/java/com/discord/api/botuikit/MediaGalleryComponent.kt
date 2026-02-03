package com.discord.api.botuikit

data class MediaGalleryComponent(
    private val type: ComponentType,
    val id: Int,
    val items: List<MediaGalleryItem>,
) : ContentComponent() {
    override fun getType() = type
}
