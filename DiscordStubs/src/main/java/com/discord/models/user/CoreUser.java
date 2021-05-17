package com.discord.models.user;

public final class CoreUser implements User {
    public String getAvatar() { return ""; }
    public int getDiscriminator() { return 0; }
    public int getFlags() { return 0; }
    public long getId() { return 0; }
    public int getPublicFlags() { return 0; }
    public String getUsername() { return ""; }
    public boolean isBot() { return false; }
    public boolean isSystemUser() { return false; }
}
