package com.discord.api.botuikit

data class TextDisplayComponent(
    private val type: ComponentType,
    val id: Int,
    val content: String,
) : ContentComponent() {
    override fun getType() = type
}
