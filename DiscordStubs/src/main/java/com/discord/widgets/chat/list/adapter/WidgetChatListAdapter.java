package com.discord.widgets.chat.list.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.discord.models.guild.Guild;
import com.discord.utilities.mg_recycler.MGRecyclerAdapterSimple;
import com.discord.utilities.mg_recycler.MGRecyclerViewHolder;
import com.discord.widgets.chat.list.entries.ChatListEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public final class WidgetChatListAdapter extends MGRecyclerAdapterSimple<ChatListEntry> {
    public interface Data {
        long getChannelId();
        Map<Long, String> getChannelNames();
        Guild getGuild();
        long getGuildId();
        List<ChatListEntry> getList();
        Set<Long> getMyRoleIds();
        long getNewMessagesMarkerMessageId();
        long getOldestMessageId();
        long getUserId();
        boolean isSpoilerClickAllowed();
    }

    public Data getData() { return null; }

    @NonNull
    @Override
    public MGRecyclerViewHolder<?, ChatListEntry> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { return onCreateViewHolder(parent, 0); }
}
