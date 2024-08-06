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
        // TODO: Calculating this statically for merged indexes might not work correctly
        var applicationCommandCounts = new HashMap<Long, Integer>();
        for (var applicationCommand: this.applicationCommands) {
            var count = applicationCommandCounts.getOrDefault(applicationCommand.applicationId, 0);
            count += 1;
            applicationCommandCounts.put(applicationCommand.applicationId, count);
        }

        var applications = new HashMap<Long, Application>();
        for (var application: this.applications) {
            applications.put(application.id, application.toModel(applicationCommandCounts.getOrDefault(application.id, 0)));
        }
        var applicationCommands = new ArrayList<ApplicationCommand>();
        for (var applicationCommand: this.applicationCommands) {
            applicationCommands.add(applicationCommand.toModel());
        }

        return new ApplicationIndex(applications, applicationCommands);
    }
}
