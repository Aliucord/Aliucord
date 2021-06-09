package com.discord.models.domain;

import androidx.annotation.Nullable;

import com.discord.api.user.User;

@SuppressWarnings("unused")
public class ModelBan implements Model {
    public long getGuildId() { return 0; }
    public User getUser() { return null; }
    @Nullable
    public String getReason() { return null; }
}
