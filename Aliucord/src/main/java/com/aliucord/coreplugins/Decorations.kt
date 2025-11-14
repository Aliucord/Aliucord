package com.aliucord.coreplugins

import android.content.Context
import android.util.AttributeSet
import com.aliucord.coreplugins.decorations.DecorationsSettings
import com.aliucord.coreplugins.decorations.Decorator
import com.aliucord.coreplugins.decorations.avatar.AvatarDecorator
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.updater.ManagerBuild
import com.aliucord.wrappers.users.*
import com.discord.api.user.User
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.models.member.GuildMember
import com.discord.models.user.CoreUser
import com.discord.models.user.MeUser
import com.discord.stores.StoreGuilds
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem
import com.discord.widgets.channels.list.items.ChannelListItemPrivate
import com.discord.widgets.channels.memberlist.adapter.*
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.discord.api.guildmember.GuildMember as ApiGuildMember

internal class Decorations : CorePlugin(Manifest().apply {
    name = "Decorations"
    description = "Adds support for various user profile decorations"
}) {
    init {
        settingsTab = SettingsTab(DecorationsSettings.Sheet::class.java, SettingsTab.Type.BOTTOM_SHEET)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private val decorators = buildList<Decorator> {
        if (DecorationsSettings.enableAvatarDecoration) add(AvatarDecorator())
    }

    override fun start(context: Context) {
        if (!ManagerBuild.hasInjector("2.3.0") || !ManagerBuild.hasPatches("1.3.0")) {
            logger.warn("Base app outdated, cannot enable Decorations")
            return
        }

        patchFields()
        patchHandlers()
        decorators.forEach { it.patch(patcher) }
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

    private fun patchHandlers() {
        // onDMsInit
        patcher.after<WidgetChannelsListAdapter.ItemChannelPrivate>(
            Int::class.javaPrimitiveType!!,
            WidgetChannelsListAdapter::class.java,
        ) { (_, _: Int, adapter: WidgetChannelsListAdapter) ->
            decorators.forEach { it.onDMsListInit(this, adapter) }
        }

        // onDMsConfigure
        patcher.after<WidgetChannelsListAdapter.ItemChannelPrivate>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChannelListItem::class.java,
        ) { (_, _: Int, item: ChannelListItemPrivate) ->
            decorators.forEach { it.onDMsListConfigure(this, item) }
        }

        // onMembersListInit
        patcher.after<ChannelMembersListViewHolderMember>(
            WidgetChannelMembersListItemUserBinding::class.java,
        ) { (_, binding: WidgetChannelMembersListItemUserBinding) ->
            decorators.forEach { it.onMembersListInit(this, binding) }
        }

        // onMembersListConfigure
        patcher.after<ChannelMembersListViewHolderMember>(
            "bind",
            ChannelMembersListAdapter.Item.Member::class.java,
            Function0::class.java,
        ) { (_, member: ChannelMembersListAdapter.Item.Member, callback: `ChannelMembersListAdapter$onBindViewHolder$1`) ->
            decorators.forEach { it.onMembersListConfigure(this, member, callback.`this$0`) }
        }

        // onMessageInit
        patcher.after<WidgetChatListAdapterItemMessage>(
            Int::class.javaPrimitiveType!!,
            WidgetChatListAdapter::class.java
        ) { (_, _: Int, adapter: WidgetChatListAdapter) ->
            decorators.forEach { it.onMessageInit(this, adapter) }
        }

        // onMessageConfigure
        patcher.after<WidgetChatListAdapterItemMessage>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: MessageEntry) ->
            decorators.forEach { it.onMessageConfigure(this, entry) }
        }

        // onProfileHeaderInit
        patcher.after<UserProfileHeaderView>(
            Context::class.java,
            AttributeSet::class.java,
        ) {
            decorators.forEach { it.onProfileHeaderInit(this) }
        }

        // onProfileHeaderConfigure
        patcher.after<UserProfileHeaderView>(
            "updateViewState",
            UserProfileHeaderViewModel.ViewState.Loaded::class.java,
        ) { (_, state: UserProfileHeaderViewModel.ViewState.Loaded) ->
            decorators.forEach { it.onProfileHeaderConfigure(this, state) }
        }
    }
}
