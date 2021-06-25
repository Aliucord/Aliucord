package com.discord.models.commands;

import com.discord.api.user.User;

@SuppressWarnings("unused")
public final class Application {
    private final int commandCount = 0;

    public Application(long id, String name, String icon, Integer iconRes, int commandCount, User bot, boolean builtIn) {}

    public final User getBot() { return null; }
    public final boolean getBuiltIn() { return false; }
    public final int getCommandCount() { return 0; }
    public final String getIcon() { return null; }
    public final Integer getIconRes() { return null; }
    public final long getId() { return 0; }
    public final String getName() { return ""; }
}
