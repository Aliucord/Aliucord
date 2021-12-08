package com.aliucord.coreplugins.welcomepage

import android.content.Context
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
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
        Patcher.addPatch(StoreAuthentication::class.java.getDeclaredMethod("handleAuthToken$${Constants.RELEASE_SUFFIX}", String::class.java), Hook {
            if (!StoreStream.getAuthentication().isAuthed && settings.getBool("showGuides", false)) return@Hook
        })
        Utils.openPageWithProxy(context, WelcomePage())
    }


    override fun stop(context: Context) {}
}
