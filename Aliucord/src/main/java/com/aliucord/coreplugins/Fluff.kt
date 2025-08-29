package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.settings.delegate
import com.aliucord.updater.ManagerBuild
import com.aliucord.wrappers.users.*
import com.discord.api.user.User
import com.discord.models.user.CoreUser
import com.discord.models.user.MeUser

internal class Fluff : CorePlugin(Manifest().apply {
    name = "Fluff"
    description = "Adds support for various user decorations"
}) {
    // Disable by default, but allows enabling for testing by manually editing the json settings
    private val SettingsAPI.enable by settings.delegate(false)

    // TODO: make visible once plugin is finished
    override val isHidden = true

    override fun start(context: Context) {
        if (!settings.enable) return
        if (!ManagerBuild.hasInjector("2.3.0") || !ManagerBuild.hasPatches("1.3.0")) {
            logger.warn("Base app outdated, cannot enable Fluff")
            return
        }
        patchFields()
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun patchFields() {
        patcher.after<CoreUser>(User::class.java) { (_, api: User) ->
            avatarDecorationData = api.avatarDecorationData
            collectibles = api.collectibles
            displayNameStyles = api.displayNameStyles
            primaryGuild = api.primaryGuild
        }
        patcher.after<CoreUser.Companion>("merge", CoreUser::class.java, User::class.java) { (param, old: CoreUser, api: User) ->
            val res = param.result as CoreUser

            (api.avatarDecorationData ?: old.avatarDecorationData)?.let { res.avatarDecorationData = it }
            (api.collectibles ?: old.collectibles)?.let { res.collectibles = it }
            (api.displayNameStyles ?: old.displayNameStyles)?.let { res.displayNameStyles = it }
            (api.primaryGuild ?: old.primaryGuild)?.let { res.primaryGuild = it }
        }

        patcher.after<MeUser>(User::class.java) { (_, api: User) ->
            avatarDecorationData = api.avatarDecorationData
            collectibles = api.collectibles
            displayNameStyles = api.displayNameStyles
            primaryGuild = api.primaryGuild
        }
        patcher.after<MeUser.Companion>("merge", MeUser::class.java, User::class.java) { (param, old: MeUser, api: User) ->
            val res = param.result as MeUser

            (api.avatarDecorationData ?: old.avatarDecorationData)?.let { res.avatarDecorationData = it }
            (api.collectibles ?: old.collectibles)?.let { res.collectibles = it }
            (api.displayNameStyles ?: old.displayNameStyles)?.let { res.displayNameStyles = it }
            (api.primaryGuild ?: old.primaryGuild)?.let { res.primaryGuild = it }
        }
    }
}
