/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import com.discord.models.commands.ApplicationCommand;
import com.discord.stores.StoreApplicationCommandsKt;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class ApiApplicationCommand {
    public final long id;
    public final long applicationId;
    public final String name;
    public final String description;
    public final List<com.discord.api.commands.ApplicationCommandOption> options;
    public final ApiPermissions permissions;
    public final Long defaultMemberPermissions;
    public final Long guildId;
    public final String version;

    public ApiApplicationCommand() {
        this.id = 0;
        this.applicationId = 0;
        this.name = null;
        this.description = null;
        this.options = null;
        this.permissions = null;
        this.defaultMemberPermissions = null;
        this.guildId = null;
        this.version = null;
    }

    public ApplicationCommand toModel() {
        var apiOptions = this.options;
        if (apiOptions == null) {
            apiOptions = new ArrayList<>();
        }
        var options = apiOptions
            .stream()
            .map(option -> StoreApplicationCommandsKt.toSlashCommandOption(option))
            .collect(Collectors.toList());
        Permissions permissions = null;
        var defaultMemberPermissions = Optional.ofNullable(this.defaultMemberPermissions);
        if (this.permissions != null) {
            permissions = this.permissions.toModel(defaultMemberPermissions);
        } else {
            permissions = new Permissions(null, null, null, defaultMemberPermissions);
        }
        return new RemoteApplicationCommand(String.valueOf(this.id), this.applicationId, this.name, this.description, options, permissions, this.guildId, this.version);
    }
}
