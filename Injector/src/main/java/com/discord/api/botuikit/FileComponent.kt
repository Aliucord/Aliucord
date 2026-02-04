package com.discord.api.botuikit

data class FileComponent(
    private val type: ComponentType,
    val id: Int,
    val file: UnfurledMediaItem,
    val spoiler: Boolean,
    val name: String,
    val size: Int,
) : ContentComponent() {
    override fun getType() = type
}
