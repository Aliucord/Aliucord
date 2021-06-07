package com.discord.stores;

import com.discord.models.guild.Guild;
import com.discord.models.member.GuildMember;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public final class StoreGuilds {
    public final Map<Long, Guild> getGuilds() { return new HashMap<>(); }
    public final Set<Long> getUnavailableGuilds() { return null; }
    public final Map<Long, Map<Long, GuildMember>> getMembers() { return new HashMap<>(); }
    public final Guild getGuild(long j) { return null; }
    public final GuildMember getMember(long j, long j2) { return null; }
}
