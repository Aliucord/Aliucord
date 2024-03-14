/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import com.discord.models.commands.ApplicationCommandOption;
import java.util.List;

class RemoteApplicationCommand extends com.discord.models.commands.RemoteApplicationCommand {
    public Permissions permissions_;

    public RemoteApplicationCommand(String id, long applicationId, String name, String description, List<ApplicationCommandOption> options, Permissions permissions, Long guildId, String version) {
        super(id, applicationId, name, description, options, guildId, version, true, null, null); // TODO: defaultPermissions
        this.permissions_ = permissions;
    }
}
