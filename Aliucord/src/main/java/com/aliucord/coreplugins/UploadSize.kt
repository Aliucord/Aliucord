package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.Plugin
import com.aliucord.patcher.instead
import com.discord.api.premium.PremiumTier
import com.discord.models.user.User
import com.discord.utilities.premium.PremiumUtils

internal class UploadSize : Plugin(Manifest("UploadSize")) {
    private companion object {
        const val DEFAULT_MAX_FILE_SIZE = 25
    }

    override fun load(context: Context?) {
        patcher.instead<PremiumUtils>("getGuildMaxFileSizeMB", Int::class.java) {
            when (it.args[0] as Int) {
                2 -> 50
                3 -> 100
                else -> DEFAULT_MAX_FILE_SIZE
            }
        }

        patcher.instead<PremiumUtils>("getMaxFileSizeMB", User::class.java) {
            when ((it.args[0] as User).premiumTier!!) {
                PremiumTier.TIER_1 -> 50 // Nitro Classic
                PremiumTier.TIER_2 -> 500 // Nitro
                else -> DEFAULT_MAX_FILE_SIZE
            }
        }
    }

    override fun start(context: Context?) {}
    override fun stop(context: Context?) {}
}
