package com.discord.api.botuikit

data class FileUploadComponent(
    private val type: ComponentType,
    val id: Int?,
    val customId: String,
    val minValues: Int,
    val maxValues: Int,
    val required: Boolean,
): ContentComponent() {
    override fun getType() = type
}
