package com.discord.api.guild;

public enum GuildVerificationLevel {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    HIGHEST(4);
    private GuildVerificationLevel(int i) { }
    public final int getApiValue() { return 0; }
}
