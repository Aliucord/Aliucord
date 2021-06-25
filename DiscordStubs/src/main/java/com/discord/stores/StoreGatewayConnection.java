package com.discord.stores;

import com.discord.api.channel.Channel;
import com.discord.api.guildmember.GuildMember;
import com.discord.api.guildmember.GuildMembersChunk;
import com.discord.api.message.Message;
import com.discord.api.user.User;
import com.discord.models.domain.ModelMessageDelete;

import java.util.List;

import rx.Observable;
import rx.subjects.SerializedSubject;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class StoreGatewayConnection {
    // events
    public final SerializedSubject<Channel, Channel> getChannelCreateOrUpdate() { return new SerializedSubject<>(); }
    public final SerializedSubject<Channel, Channel> getChannelDeleted() { return new SerializedSubject<>(); }
    public final Observable<Boolean> getConnected() { return new Observable<>(); }
    public final Observable<Boolean> getConnectionReady() { return new Observable<>(); }
    public final SerializedSubject<GuildMember, GuildMember> getGuildMembersAdd() { return new SerializedSubject<>(); }
    public final SerializedSubject<GuildMembersChunk, GuildMembersChunk> getGuildMembersChunk() { return new SerializedSubject<>(); }
    public final SerializedSubject<Message, Message> getMessageCreate() { return new SerializedSubject<>(); }
    public final SerializedSubject<ModelMessageDelete, ModelMessageDelete> getMessageDelete() { return new SerializedSubject<>(); }
    public final SerializedSubject<Message, Message> getMessageUpdate() { return new SerializedSubject<>(); }
    public final SerializedSubject<User, User> getUserUpdate() { return new SerializedSubject<>(); }

    public boolean requestGuildMembers(long guildId, String query, List<Long> userIds) { return false; }
}
