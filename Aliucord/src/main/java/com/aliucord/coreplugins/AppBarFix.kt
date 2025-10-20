package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.instead
import com.discord.widgets.chat.input.SmoothKeyboardReactionHelper

internal class AppBarFix : CorePlugin(Manifest("AppBarFix")) {
    init {
        manifest.description = "Fixes erratic AppBarLayout behavior by disabling 'smooth keyboard' animation"
    }

    override fun start(context: Context) {
        patcher.instead<SmoothKeyboardReactionHelper>("install", View::class.java) {}
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
