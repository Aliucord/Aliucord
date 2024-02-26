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
import com.aliucord.utils.ReflectUtils;
import com.discord.api.commands.Application;
import com.discord.api.commands.ApplicationCommand;
import com.discord.api.commands.GuildApplicationCommands;
import com.discord.models.deserialization.gson.InboundGatewayGsonParser;
import com.discord.stores.StoreApplicationCommands;
import com.discord.stores.StoreApplicationCommands$requestApplicationCommands$1;
import com.discord.stores.StoreApplicationCommands$requestApplicationCommandsQuery$1;
import com.discord.stores.StoreApplicationCommands$requestApplications$1;
import com.discord.stores.StoreApplicationCommandsKt;
import com.discord.stores.StoreStream;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

final class SlashCommandsFix extends Plugin {
    private class CachedGuild {
        public GuildApplicationCommands applicationIndex;

        CachedGuild(GuildApplicationCommands applicationIndex) {
            this.applicationIndex = applicationIndex;
        }
    }

    private enum RequestSource {
        GUILD,
        BROWSE,
        QUERY;
    }

    private HashMap<Long, CachedGuild> cachedGuilds;
    private Gson gson;

    SlashCommandsFix() {
        super(new Manifest("SlashCommandsFix"));

        this.cachedGuilds = new HashMap<Long, CachedGuild>();
    }

    @Override
    public void start(Context context) throws Throwable {
        this.gson = (Gson) ReflectUtils.getField(InboundGatewayGsonParser.class, null, "gatewayGsonInstance");

        var storeApplicationCommands = StoreStream.getApplicationCommands();

        // Browsing commands (when just a '/' is typed)
        Patcher.addPatch(
            StoreApplicationCommands$requestApplicationCommands$1.class.getDeclaredMethod("invoke"),
            new PreHook(param -> {
                var this_ = (StoreApplicationCommands$requestApplicationCommands$1) param.thisObject;
                if (this_.$applicationId == null) {
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

        GuildApplicationCommands applicationIndex = null;
        var cachedGuild = this.cachedGuilds.get(guildId);
        if (cachedGuild != null) {
            // Reuse cached guild
            applicationIndex = cachedGuild.applicationIndex;
        } else {
            try {
                // Request application index
                applicationIndex = Http.Request.newDiscordRNRequest(String.format("/guilds/%d/application-command-index", guildId))
                    .execute()
                    .json(gson, GuildApplicationCommands.class);

                // Fix up attributes that are needed by the client but aren't sent via the new API

                // Guild ID
                ReflectUtils.setField(applicationIndex, "guildId", guildId);

                // Command count
                var applications = (List<Application>) ReflectUtils.getField(applicationIndex, "applications");
                var applicationCommands = (List<ApplicationCommand>) ReflectUtils.getField(applicationIndex, "applicationCommands");
                for (var application: applications) {
                    var applicationId = (long) ReflectUtils.getField(application, "id");
                    // TODO: Calculate this for all commands beforehand so there's no nested loop
                    var commandCount = applicationCommands
                        .stream()
                        .filter(applicationCommand -> {
                            try {
                                return (long) ReflectUtils.getField(applicationCommand, "applicationId") == applicationId;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .count();
                    ReflectUtils.setField(application, "commandCount", (int) commandCount);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            this.cachedGuilds.put(guildId, new CachedGuild(applicationIndex));
        }

        // Pass the information to StoreApplicationCommands
        if (requestSource == RequestSource.GUILD) {
            var applications = new ArrayList<com.discord.models.commands.Application>();
            for (var application: applicationIndex.b()) {
                applications.add(StoreApplicationCommandsKt.toDomainApplication(application));
            }
            try {
                var handleGuildApplicationsUpdateMethod = StoreApplicationCommands.class.getDeclaredMethod("handleGuildApplicationsUpdate", List.class);
                handleGuildApplicationsUpdateMethod.setAccessible(true);
                handleGuildApplicationsUpdateMethod.invoke(storeApplicationCommands, applications);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (requestSource == RequestSource.BROWSE || requestSource == RequestSource.QUERY) {
            var applicationCommands = new ArrayList<com.discord.models.commands.ApplicationCommand>();
            for (var applicationCommand: applicationIndex.a()) {
                applicationCommands.add(StoreApplicationCommandsKt.toSlashCommand(applicationCommand));
            }
            try {
                if (requestSource == RequestSource.BROWSE) {
                    var handleDiscoverCommandsUpdateMethod = StoreApplicationCommands.class.getDeclaredMethod("handleDiscoverCommandsUpdate", List.class);
                    handleDiscoverCommandsUpdateMethod.setAccessible(true);
                    handleDiscoverCommandsUpdateMethod.invoke(storeApplicationCommands, applicationCommands);
                } else if (requestSource == RequestSource.QUERY) {
                    var handleQueryCommandsUpdateMethod = StoreApplicationCommands.class.getDeclaredMethod("handleQueryCommandsUpdate", List.class);
                    handleQueryCommandsUpdateMethod.setAccessible(true);
                    handleQueryCommandsUpdateMethod.invoke(storeApplicationCommands, applicationCommands);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
