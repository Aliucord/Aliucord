package com.discord.api.botuikit

abstract class SelectV2Component() : ActionComponent() {
    abstract val id: Int
    abstract val customId: String
    abstract val placeholder: String
    abstract val defaultValues: List<SelectV2DefaultValue>?
    abstract val minValues: Int
    abstract val maxValues: Int
    abstract val disabled: Boolean
}
