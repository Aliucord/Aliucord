package com.discord.api.guild;

public enum GuildExplicitContentFilter {
    NONE(0),
    SOME(1),
    ALL(2);
    private GuildExplicitContentFilter(int i) { }
    public final int getApiValue() { return 0; }
}
