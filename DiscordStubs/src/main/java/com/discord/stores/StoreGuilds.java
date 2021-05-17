package com.discord.stores;

import com.discord.models.guild.Guild;
import com.discord.models.member.GuildMember;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class StoreGuilds {
    public final Map<Long, Guild> getGuilds() { return new HashMap<>(); }
    public final Map<Long, Map<Long, GuildMember>> getMembers() { return new HashMap<>(); }
}
