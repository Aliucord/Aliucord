/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import com.aliucord.Logger;
import com.discord.models.user.User;
import com.discord.utilities.user.UserUtils;
import java.util.Optional;

class Application extends com.discord.models.commands.Application {
    public Permissions permissions_;

    public Application(long id, String name, String icon, Permissions permissions, int commandCount, Optional<User> botUser) {
        super(id, name, icon, null, commandCount, botUser.map(user -> UserUtils.INSTANCE.synthesizeApiUser(user)).orElse(null), false);
        this.permissions_ = permissions;
    }
}
