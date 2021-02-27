package com.discord.models.domain;

@SuppressWarnings("unused")
public class ModelChannel {
    public static class RecipientNick {}

    public boolean isDM() { return false; }
    public boolean isGroup() { return false; }

    public Long getGuildId() { return null; }
    public String getName() { return null; }
}
