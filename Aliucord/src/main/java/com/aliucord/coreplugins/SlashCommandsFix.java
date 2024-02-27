/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.content.Context;
import com.aliucord.entities.Plugin;
import com.aliucord.Http;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PreHook;
import com.aliucord.utils.GsonUtils;
import com.aliucord.utils.ReflectUtils;
import com.discord.models.commands.Application;
import com.discord.models.commands.ApplicationCommand;
import com.discord.models.commands.ApplicationCommandOption;
import com.discord.models.commands.RemoteApplicationCommand;
import com.discord.stores.StoreApplicationCommands;
import com.discord.stores.StoreApplicationCommands$requestApplicationCommands$1;
import com.discord.stores.StoreApplicationCommands$requestApplicationCommandsQuery$1;
import com.discord.stores.StoreApplicationCommands$requestApplications$1;
import com.discord.stores.StoreApplicationCommandsKt;
import com.discord.stores.StoreStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class SlashCommandsFix extends Plugin {
    private class ApiApplication {
        public final long id;
        public final String name;
        public final String icon;

        public ApiApplication() {
            this.id = 0;
            this.name = null;
            this.icon = null;
        }

        public Application toModel(int commandCount) {
            return new Application(this.id, this.name, this.icon, null, commandCount, null, false);
        }
    }

    private class ApiApplicationCommand {
        public final long id;
        public final long applicationId;
        public final String name;
        public final String description;
        public final List<com.discord.api.commands.ApplicationCommandOption> options;
        public final String version;

        public ApiApplicationCommand() {
            this.id = 0;
            this.applicationId = 0;
            this.name = null;
            this.description = null;
            this.options = null;
            this.version = null;
        }

        public ApplicationCommand toModel() {
            var apiOptions = this.options;
            if (apiOptions == null) {
                apiOptions = new ArrayList();
            }
            var options = apiOptions
                .stream()
                .map(option -> StoreApplicationCommandsKt.toSlashCommandOption(option))
                .collect(Collectors.toList());
            return new RemoteApplicationCommand(String.valueOf(this.id), this.applicationId, this.name, this.description, options, null, this.version, true, new ArrayList(), null);
        }
    }

    private class ApiApplicationIndex {
        public List<ApiApplication> applications;
        public List<ApiApplicationCommand> applicationCommands;

        public ApiApplicationIndex() {
            this.applications = null;
            this.applicationCommands = null;
        }

        public ApplicationIndex toModel() {
            var applicationCommandCounts = new HashMap<Long, Integer>();
            for (var applicationCommand: this.applicationCommands) {
                var count = applicationCommandCounts.getOrDefault(applicationCommand.applicationId, 0);
                count += 1;
                applicationCommandCounts.put(applicationCommand.applicationId, count);
            }

            var applications = new ArrayList<Application>();
            for (var application: this.applications) {
                applications.add(application.toModel(applicationCommandCounts.getOrDefault(application.id, 0)));
            }
            var applicationCommands = new ArrayList<ApplicationCommand>();
            for (var applicationCommand: this.applicationCommands) {
                applicationCommands.add(applicationCommand.toModel());
            }

            return new ApplicationIndex(applications, applicationCommands);
        }
    }

    private class ApplicationIndex {
        public List<Application> applications;
        public List<ApplicationCommand> applicationCommands;

        public ApplicationIndex(List<Application> applications, List<ApplicationCommand> applicationCommands) {
            this.applications = applications;
            this.applicationCommands = applicationCommands;
        }
    }

    private enum RequestSource {
        GUILD,
        BROWSE,
        QUERY;
    }

    private Map<Long, ApplicationIndex> guildApplicationIndexes;

    SlashCommandsFix() {
        super(new Manifest("SlashCommandsFix"));

        this.guildApplicationIndexes = new HashMap();
    }

    @Override
    public void start(Context context) throws Throwable {
        var storeApplicationCommands = StoreStream.getApplicationCommands();

        // Browsing commands (when just a '/' is typed)
        Patcher.addPatch(
            StoreApplicationCommands$requestApplicationCommands$1.class.getDeclaredMethod("invoke"),
            new PreHook(param -> {
                var this_ = (StoreApplicationCommands$requestApplicationCommands$1) param.thisObject;
                if (this_.$guildId == null) {
                    return;
                }

                this.passCommandData(this_.this$0, this_.$guildId, RequestSource.BROWSE);
                param.setResult(null);
            })
        );

        // Requesting applications present in the guild
        Patcher.addPatch(
            StoreApplicationCommands$requestApplications$1.class.getDeclaredMethod("invoke"),
            new PreHook(param -> {
                var this_ = (StoreApplicationCommands$requestApplications$1) param.thisObject;
                if (this_.$guildId == null) {
                    return;
                }

                this.passCommandData(this_.this$0, this_.$guildId, RequestSource.GUILD);
                param.setResult(null);
            })
        );

        // Autocompleting commands
        Patcher.addPatch(
            StoreApplicationCommands$requestApplicationCommandsQuery$1.class.getDeclaredMethod("invoke"),
            new PreHook(param -> {
                var this_ = (StoreApplicationCommands$requestApplicationCommandsQuery$1) param.thisObject;
                if (this_.$guildId == null) {
                    return;
                }

                this.passCommandData(this_.this$0, this_.$guildId, RequestSource.QUERY);
                param.setResult(null);
            })
        );
    }

    @Override
    public void stop(Context context) {}

    // Upcasting Object generates a warning and we need that to get private fields with reflection
    @SuppressWarnings("unchecked")
    private void passCommandData(StoreApplicationCommands storeApplicationCommands, long guildId, RequestSource requestSource) {
        // TODO: Cache the fields as they are requested every time this runs

        // Reuse application index from cache
        var applicationIndex = this.guildApplicationIndexes.get(guildId);
        if (applicationIndex == null) {
            try {
                // Request application index from API
                applicationIndex = Http.Request.newDiscordRNRequest(String.format("/guilds/%d/application-command-index", guildId))
                    .execute()
                    .json(GsonUtils.getGsonRestApi(), ApiApplicationIndex.class)
                    .toModel();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            this.guildApplicationIndexes.put(guildId, applicationIndex);
        }

        // Pass the information to StoreApplicationCommands
        if (requestSource == RequestSource.GUILD) {
            try {
                var handleGuildApplicationsUpdateMethod = StoreApplicationCommands.class.getDeclaredMethod("handleGuildApplicationsUpdate", List.class);
                handleGuildApplicationsUpdateMethod.setAccessible(true);
                handleGuildApplicationsUpdateMethod.invoke(storeApplicationCommands, applicationIndex.applications);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (requestSource == RequestSource.BROWSE || requestSource == RequestSource.QUERY) {
            try {
                if (requestSource == RequestSource.BROWSE) {
                    var handleDiscoverCommandsUpdateMethod = StoreApplicationCommands.class.getDeclaredMethod("handleDiscoverCommandsUpdate", List.class);
                    handleDiscoverCommandsUpdateMethod.setAccessible(true);
                    handleDiscoverCommandsUpdateMethod.invoke(storeApplicationCommands, applicationIndex.applicationCommands);
                } else if (requestSource == RequestSource.QUERY) {
                    var handleQueryCommandsUpdateMethod = StoreApplicationCommands.class.getDeclaredMethod("handleQueryCommandsUpdate", List.class);
                    handleQueryCommandsUpdateMethod.setAccessible(true);
                    handleQueryCommandsUpdateMethod.invoke(storeApplicationCommands, applicationIndex.applicationCommands);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
