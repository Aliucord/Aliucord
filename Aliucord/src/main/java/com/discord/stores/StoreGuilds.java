package com.discord.stores;

import com.discord.models.guild.Guild;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StoreGuilds {
    public Map<Long, Guild> getGuilds() {
        return new HashMap<>();
    }
}
