package com.aliucord.coreplugins.welcomepage

import android.content.Context
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.api.PatcherAPI
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.discord.models.domain.auth.ModelLoginResult
import com.discord.stores.StoreAuthentication
import com.discord.stores.StoreStream

internal class WelcomeScreen : Plugin() {

    init {
        Manifest().run {
            name = "WelcomePage"
            initialize(this)
        }
    }

    override fun start(context: Context) {
        Patcher.addPatch(StoreAuthentication::class.java.getDeclaredMethod("handleLoginResult", ModelLoginResult::class.java), Hook {
            if (settings.getBool("hasShownWelcome", false)) return@Hook
            Utils.openPageWithProxy(context, WelcomePage())
        })
    }

    override fun stop(context: Context) {}
}
