package com.discord.models.user;

@SuppressWarnings("unused")
public interface User {
    String getAvatar();

    int getDiscriminator();

    int getFlags();

    long getId();

    int getPublicFlags();

    String getUsername();

    boolean isBot();

    boolean isSystemUser();
}
