package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.coreplugins.decorations.DecorationsSettings
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.updater.ManagerBuild
import com.aliucord.wrappers.users.*
import com.discord.api.user.User
import com.discord.models.member.GuildMember
import com.discord.models.user.CoreUser
import com.discord.models.user.MeUser
import com.discord.stores.StoreGuilds
import com.discord.api.guildmember.GuildMember as ApiGuildMember

internal class Decorations : CorePlugin(Manifest().apply {
    name = "Decorations"
    description = "Adds support for various user profile decorations"
}) {
    // TODO: make visible once plugin is ready
    override val isHidden = true

    init {
        settingsTab = SettingsTab(DecorationsSettings.Sheet::class.java, SettingsTab.Type.BOTTOM_SHEET)
    }

    override fun start(context: Context) {
        if (!DecorationsSettings.enable) return
        if (!ManagerBuild.hasInjector("2.3.0") || !ManagerBuild.hasPatches("1.3.0")) {
            logger.warn("Base app outdated, cannot enable Decorations")
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

        // The signature is huge, and it's a static method so can't be patched by patcher.after due to a bug
        // Therefore I (Lava) have opted to do this instead
        // This method would be ApiGuildMember.copy$default
        patcher.patch(ApiGuildMember::class.java.declaredMethods.first { it.name == "a" }) { (param, old: ApiGuildMember) ->
            val res = param.result as ApiGuildMember
            res.avatarDecorationData = old.avatarDecorationData
            res.collectibles = old.collectibles
        }

        patcher.after<GuildMember.Companion>(
            "from",
            ApiGuildMember::class.java,
            Long::class.javaPrimitiveType!!,
            Map::class.java,
            StoreGuilds::class.java,
        ) { (param, api: ApiGuildMember) ->
            val res = param.result as GuildMember

            res.avatarDecorationData = api.avatarDecorationData
            res.collectibles = api.collectibles
        }
    }
}
