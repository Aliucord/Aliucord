package com.discord.api.botuikit

data class MentionableSelectComponent(
    private val type: ComponentType,
    override val id: Int,
    override val customId: String,
    override val placeholder: String,
    override val defaultValues: List<SelectV2DefaultValue>?,
    override val minValues: Int,
    override val maxValues: Int,
    override val disabled: Boolean,
) : SelectV2Component() {
    override fun getType() = type
}
