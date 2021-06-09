package com.discord.api.message.activity;

public enum MessageActivityType {
    JOIN(1),
    SPECTATE(2),
    LISTEN(3),
    WATCH(4),
    REQUEST(5);

    private MessageActivityType(int i) { }
    public final int getApiInt() { return 0; }
}
