package com.aliucord.coreplugins.decorations.avatar

import com.discord.api.sticker.*
import com.discord.api.user.AvatarDecoration

// We're using a StickerView for avatar decorations because it is the simplest way to get animated APNGs
data class AvatarSticker(val data: AvatarDecoration) : BaseSticker {
    override fun a(): StickerFormatType = StickerFormatType.APNG
    override fun b(): String = ".png"
    override fun c(): StickerPartial = throw UnsupportedOperationException("Unreachable") // This should never be called
    override fun d(): Long = data.skuId
}
