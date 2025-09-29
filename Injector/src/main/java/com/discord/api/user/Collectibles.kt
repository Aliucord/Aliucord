package com.discord.api.user

data class Collectibles(
    val nameplate: Nameplate?,
) {
    data class Nameplate(
        val asset: String,
        val skuId: Long,
        val label: String,
        val palette: String,
        val expiresAt: Int?,
    )
}
