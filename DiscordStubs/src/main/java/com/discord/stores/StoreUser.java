package com.discord.stores;

import com.discord.models.user.MeUser;
import com.discord.models.user.User;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class StoreUser {
    public final MeUser getMe() { return new MeUser(); }
    public final Map<Long, User> getUsers() { return new HashMap<>(); }
}
