package com.discord.widgets.chat.list.entries;

import com.discord.models.message.Message;
import com.discord.stores.StoreMessageState;

@SuppressWarnings({ "unused", "InfiniteRecursion" })
public final class MessageEntry extends ChatListEntry {
    @Override
    public String getKey() { return null; }
    @Override
    public int getType() { return 0; }

    public final Message getMessage() { return getMessage(); }
    public final StoreMessageState.State getMessageState() { return getMessageState(); }
}
