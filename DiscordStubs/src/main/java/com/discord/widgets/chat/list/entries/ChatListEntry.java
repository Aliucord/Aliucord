package com.discord.widgets.chat.list.entries;

import com.discord.utilities.mg_recycler.MGRecyclerDataPayload;

@SuppressWarnings("unused")
public abstract class ChatListEntry implements MGRecyclerDataPayload {
    public boolean isInExpandedBlockedMessageChunk() { return false; }
}
