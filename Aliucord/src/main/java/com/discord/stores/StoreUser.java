package com.discord.stores;

import com.discord.models.user.MeUser;

@SuppressWarnings("unused")
public class StoreUser {
    public MeUser getMe() { return new MeUser(); }
}
