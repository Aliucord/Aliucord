package com.discord.api.botuikit

data class SeparatorComponent(
    private val type: ComponentType,
    val id: Int,
    val divider: Boolean,
    val spacing: Int, // 1 = small padding, 2 = large padding
): LayoutComponent() {
    override fun getType() = type
    override fun a(): List<Component> = listOf()
}
