/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import com.discord.models.commands.ApplicationCommand;
import java.util.List;
import java.util.Map;

class ApplicationIndex {
    public Map<Long, Application> applications;
    public Map<Long, ApplicationCommand> applicationCommands;

    public ApplicationIndex(Map<Long, Application> applications, Map<Long, ApplicationCommand> applicationCommands) {
        this.applications = applications;
        this.applicationCommands = applicationCommands;
    }
}
