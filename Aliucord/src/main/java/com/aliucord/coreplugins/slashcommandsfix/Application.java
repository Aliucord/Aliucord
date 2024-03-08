/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

class Application extends com.discord.models.commands.Application {
    public Permissions permissions_;

    public Application(long id, String name, String icon, Permissions permissions, int commandCount) {
        super(id, name, icon, null, commandCount, null, false);
        this.permissions_ = permissions;
    }
}
