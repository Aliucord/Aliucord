package com.discord.widgets.chat.list.adapter;

import com.discord.utilities.mg_recycler.MGRecyclerViewHolder;
import com.discord.widgets.chat.list.entries.ChatListEntry;

@SuppressWarnings("unused")
public class WidgetChatListItem extends MGRecyclerViewHolder<WidgetChatListAdapter, ChatListEntry> {
    public WidgetChatListItem(int id, WidgetChatListAdapter widgetChatListAdapter) {
        super(id, widgetChatListAdapter);
    }
}
