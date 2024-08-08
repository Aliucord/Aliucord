/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import com.discord.models.commands.ApplicationCommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class ApiApplicationIndex {
    public List<ApiApplication> applications;
    public List<ApiApplicationCommand> applicationCommands;

    public ApiApplicationIndex() {
        this.applications = null;
        this.applicationCommands = null;
    }

    public ApplicationIndex toModel() {
        var applications = new HashMap<Long, Application>();
        for (var application: this.applications) {
            applications.put(application.id, application.toModel());
        }
        var applicationCommands = new HashMap<Long, ApplicationCommand>();
        for (var applicationCommand: this.applicationCommands) {
            applicationCommands.put(applicationCommand.id, applicationCommand.toModel());
        }

        return new ApplicationIndex(applications, applicationCommands);
    }
}
