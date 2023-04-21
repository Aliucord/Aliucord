package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.discord.api.premium.PremiumTier
import com.discord.models.user.User
import com.discord.utilities.premium.PremiumUtils
import de.robv.android.xposed.XposedBridge
import b.a.a.c as ImageUploadFailedDialog

internal class UploadSize : Plugin(Manifest("UploadSize")) {
    private companion object {
        const val DEFAULT_MAX_FILE_SIZE = 25
    }

    override fun load(context: Context?) {
        patcher.instead<PremiumUtils>("getGuildMaxFileSizeMB", Int::class.java) { (_, tier: Int) ->
            when (tier) {
                2 -> 50
                3 -> 100
                else -> DEFAULT_MAX_FILE_SIZE
            }
        }

        patcher.instead<PremiumUtils>("getMaxFileSizeMB", User::class.java) { (_, user: User) ->
            when (user.premiumTier!!) {
                PremiumTier.TIER_1 -> 50 // Nitro Classic
                PremiumTier.TIER_2 -> 500 // Nitro
                else -> DEFAULT_MAX_FILE_SIZE
            }
        }

        patcher.instead<ImageUploadFailedDialog>("onViewBound", View::class.java) {
            val maxFileSize = argumentsOrDefault.getInt("PARAM_MAX_FILE_SIZE_MB")

            argumentsOrDefault.putInt("PARAM_MAX_FILE_SIZE_MB", 8)

            XposedBridge.invokeOriginalMethod(it.method, it.thisObject, it.args)

            @Suppress("SetTextI18n")
            g().j.text = "Max file size is $maxFileSize MB"

            null
        }
    }

    override fun start(context: Context?) {}
    override fun stop(context: Context?) {}
}
