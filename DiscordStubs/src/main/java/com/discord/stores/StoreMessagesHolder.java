package com.discord.stores;

import com.discord.models.domain.ModelMessage;

import java.util.TreeMap;

@SuppressWarnings("unused")
public class StoreMessagesHolder {
    public TreeMap<Long, ModelMessage> getMessagesForChannel(Long id) { return null; }

    @SuppressWarnings("SameParameterValue")
    private void publishIfUpdated(boolean force) {}
    private void publishIfUpdated() { publishIfUpdated(false); }
}
