package com.discord.stores;

import com.discord.models.domain.ModelMessage;

import java.util.List;

import rx.subjects.SerializedSubject;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class StoreGatewayConnection {
    public final SerializedSubject<ModelMessage, ModelMessage> getMessageCreate() { return new SerializedSubject<>(); }
    public final SerializedSubject<ModelMessage, ModelMessage> getMessageUpdate() { return new SerializedSubject<>(); }

    public boolean requestGuildMembers(List<Long> guildIds, String query, List<Long> userIds) { return false; }
}
