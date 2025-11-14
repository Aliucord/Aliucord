package com.aliucord.coreplugins.decorations

import com.aliucord.api.PatcherAPI
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItemPrivate
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel

internal abstract class Decorator {
    open fun patch(patcher: PatcherAPI) {}

    open fun onDMsListInit(
        holder: WidgetChannelsListAdapter.ItemChannelPrivate,
        adapter: WidgetChannelsListAdapter
    ) {}
    open fun onDMsListConfigure(
        holder: WidgetChannelsListAdapter.ItemChannelPrivate,
        item: ChannelListItemPrivate
    ) {}

    open fun onMembersListInit(
        holder: ChannelMembersListViewHolderMember,
        binding: WidgetChannelMembersListItemUserBinding
    ) {}
    open fun onMembersListConfigure(
        holder: ChannelMembersListViewHolderMember,
        item: ChannelMembersListAdapter.Item.Member,
        adapter: ChannelMembersListAdapter
    ) {}

    open fun onMessageInit(
        holder: WidgetChatListAdapterItemMessage,
        adapter: WidgetChatListAdapter
    ) {}
    open fun onMessageConfigure(
        holder: WidgetChatListAdapterItemMessage,
        entry: MessageEntry
    ) {}

    open fun onProfileHeaderInit(view: UserProfileHeaderView) {}
    open fun onProfileHeaderConfigure(
        view: UserProfileHeaderView,
        state: UserProfileHeaderViewModel.ViewState.Loaded
    ) {}
}
