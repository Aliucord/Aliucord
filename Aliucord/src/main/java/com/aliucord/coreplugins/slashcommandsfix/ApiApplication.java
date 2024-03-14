/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import com.discord.models.user.User;
import com.discord.stores.StoreStream;
import java.util.Optional;

class ApiApplication {
    public final long id;
    public final String name;
    public final String icon;
    public final ApiPermissions permissions;
    public final Long botId;

    public ApiApplication() {
        this.id = 0;
        this.name = null;
        this.icon = null;
        this.permissions = null;
        this.botId = null;
    }

    public Application toModel(int commandCount) {
        Permissions permissions = null;
        if (this.permissions != null) {
            permissions = this.permissions.toModel();
        } else {
            permissions = new Permissions(null, null, null);
        }
        var usersStore = StoreStream.getUsers();
        Optional<User> botUser = Optional.ofNullable(this.botId).map(userId -> usersStore.getUsers().get(userId));
        return new Application(this.id, this.name, this.icon, permissions, commandCount, botUser);
    }
}
