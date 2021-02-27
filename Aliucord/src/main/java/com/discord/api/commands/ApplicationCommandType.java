package com.discord.api.commands;

@SuppressWarnings("unused")
public enum ApplicationCommandType {
    SUBCOMMAND(1),
    SUBCOMMAND_GROUP(2),
    STRING(3),
    INTEGER(4),
    BOOLEAN(5),
    USER(6),
    CHANNEL(7),
    ROLE(8),
    UNKNOWN(0);

    private final int type;

    ApplicationCommandType(int i) {
        this.type = i;
    }

    public final int getType() {
        return this.type;
    }
}
