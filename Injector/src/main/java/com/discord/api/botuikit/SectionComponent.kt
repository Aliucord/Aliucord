package com.discord.api.botuikit

data class SectionComponent(
    private val type: ComponentType,
    val id: Int,
    val components: List<Component>,
    val accessory: Component,
): LayoutComponent() {
    override fun getType() = type

    // This property will be accessed by ComponentStateMapper to be processed into MessageComponents,
    // so we pass in the accessory component to be processed too.
    // Back in SectionMessageComponent.mergeToMessageComponent, we will separate this back correctly.
    override fun a() = components + accessory
}
