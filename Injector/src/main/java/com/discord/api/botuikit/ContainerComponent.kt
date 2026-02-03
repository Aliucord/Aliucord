package com.discord.api.botuikit

data class ContainerComponent(
    private val type: ComponentType,
    val id: Int,
    val components: List<Component>,
    val accentColor: Int?,
    val spoiler: Boolean,
): LayoutComponent() {
    override fun getType() = type
    override fun a() = components
}
