package com.discord.stores;

import com.discord.api.message.Message;

import java.util.TreeMap;

@SuppressWarnings("unused")
public class StoreMessagesHolder {
    public TreeMap<Long, Message> getMessagesForChannel(Long id) { return null; }

    @SuppressWarnings("SameParameterValue")
    private void publishIfUpdated(boolean force) {}
    private void publishIfUpdated() { publishIfUpdated(false); }
}
