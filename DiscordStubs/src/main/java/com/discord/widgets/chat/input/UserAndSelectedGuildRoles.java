package com.discord.widgets.chat.input;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("unused")
public final class UserAndSelectedGuildRoles {
    public UserAndSelectedGuildRoles(long userId, @NonNull List<Long> userSelectedGuildRoles) { }

    public final long getUserId() { return 0; }
    @NonNull
    public final List<Long> getUserSelectedGuildRoles() { return new ArrayList<>(); }
}
