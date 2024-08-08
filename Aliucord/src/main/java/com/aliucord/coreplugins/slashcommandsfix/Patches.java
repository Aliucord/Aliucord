/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import android.content.Context;
import com.aliucord.api.GatewayAPI;
import com.aliucord.Http;
import com.aliucord.Logger;
import com.aliucord.patcher.InsteadHook;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PreHook;
import com.aliucord.Utils;
import com.aliucord.utils.GsonUtils;
import com.aliucord.utils.ReflectUtils;
import com.discord.models.commands.Application;
import com.discord.models.commands.ApplicationCommand;
import com.discord.models.commands.ApplicationCommandKt;
import com.discord.models.commands.ApplicationCommandLocalSendData;
import com.discord.stores.BuiltInCommandsProvider;
import com.discord.stores.StoreApplicationCommands;
import com.discord.stores.StoreApplicationCommands$handleDmUserApplication$1;
import com.discord.stores.StoreApplicationCommands$requestApplicationCommands$1;
import com.discord.stores.StoreApplicationCommands$requestApplicationCommandsQuery$1;
import com.discord.stores.StoreApplicationCommands$requestApplications$1;
import com.discord.stores.StoreApplicationInteractions;
import com.discord.stores.StoreStream;
import com.discord.utilities.error.Error;
import com.discord.utilities.messagesend.MessageResult;
import com.discord.utilities.permissions.PermissionUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

final class Patches {
    private ApplicationIndexCache applicationIndexCache;
    private Logger logger;
    private Method handleGuildApplicationsUpdateMethod;
    private Method handleDiscoverCommandsUpdateMethod;
    private Method handleQueryCommandsUpdateMethod;
    private Field applicationCommandCountField;

    Patches(Logger logger) throws Throwable {
        this.logger = logger;
        this.applicationIndexCache = new ApplicationIndexCache();
    }

    @SuppressWarnings("unchecked")
    public void loadPatches(Context context) throws Throwable {
        this.handleGuildApplicationsUpdateMethod = StoreApplicationCommands.class.getDeclaredMethod("handleGuildApplicationsUpdate", List.class);
        this.handleGuildApplicationsUpdateMethod.setAccessible(true);
        this.handleDiscoverCommandsUpdateMethod = StoreApplicationCommands.class.getDeclaredMethod("handleDiscoverCommandsUpdate", List.class);
        this.handleDiscoverCommandsUpdateMethod.setAccessible(true);
        this.handleQueryCommandsUpdateMethod = StoreApplicationCommands.class.getDeclaredMethod("handleQueryCommandsUpdate", List.class);
        this.handleQueryCommandsUpdateMethod.setAccessible(true);
        this.applicationCommandCountField = Application.class.getDeclaredField("commandCount");
        this.applicationCommandCountField.setAccessible(true);

        var storeApplicationCommands = StoreStream.getApplicationCommands();
        var storeChannelsSelected = StoreStream.getChannelsSelected();
        var storeUsers = StoreStream.getUsers();
        var storePermissions = StoreStream.getPermissions();
        var storeGuilds = StoreStream.getGuilds();

        // Requesting applications present in the guild
        Patcher.addPatch(
            StoreApplicationCommands$requestApplications$1.class.getDeclaredMethod("invoke"),
            new PreHook(param -> {
                var this_ = (StoreApplicationCommands$requestApplications$1) param.thisObject;

                if (this_.$guildId == null) {
                    return;
                }

                try {
                    this.passCommandData(this_.this$0, Optional.of(new ApplicationIndexSourceGuild(this_.$guildId)), RequestSource.INITIAL);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                param.setResult(null);
            })
        );

        // Requesting applications present in the DM (only gets called for bots)
        Patcher.addPatch(
            StoreApplicationCommands$handleDmUserApplication$1.class.getDeclaredMethod("invoke"),
            new InsteadHook(param -> {
                var this_ = (StoreApplicationCommands$handleDmUserApplication$1) param.thisObject;

                var channelId = storeChannelsSelected
                    .getSelectedChannel()
                    .k();

                try {
                    this.passCommandData(this_.this$0, Optional.of(new ApplicationIndexSourceDm(channelId)), RequestSource.INITIAL);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return null;
            })
        );

        // Browsing commands (when just a '/' is typed)
        Patcher.addPatch(
            StoreApplicationCommands$requestApplicationCommands$1.class.getDeclaredMethod("invoke"),
            new PreHook(param -> {
                var this_ = (StoreApplicationCommands$requestApplicationCommands$1) param.thisObject;

                if (this_.$guildId == null) {
                    return;
                }

                Optional<ApplicationIndexSource> applicationIndexSource = Optional.empty();
                // guildId being 0 means this is a DM or a DM group
                if (this_.$guildId != 0) {
                    applicationIndexSource = Optional.of(new ApplicationIndexSourceGuild(this_.$guildId));
                } else {
                    // Only create a DM index source for bots
                    var channel = storeChannelsSelected.getSelectedChannel();
                    var channelType = channel.D();
                    // Channel type 1 is a DM
                    if (channelType == 1) {
                        var user = channel.z().get(0);
                        var userIsBot = user.e();
                        if (userIsBot) {
                            var channelId = channel.k();
                            applicationIndexSource = Optional.of(new ApplicationIndexSourceDm(channelId));
                        }
                    }

                    if (!applicationIndexSource.isPresent()) {
                        return;
                    }
                }

                try {
                    this.passCommandData(this_.this$0, applicationIndexSource, RequestSource.BROWSE);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

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

                try {
                    ReflectUtils.setField(this_.this$0, "query", this_.$query);
                    this.passCommandData(this_.this$0, Optional.of(new ApplicationIndexSourceGuild(this_.$guildId)), RequestSource.QUERY);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                param.setResult(null);
            })
        );

        // Command permission check
        Patcher.addPatch(
            ApplicationCommandKt.class.getDeclaredMethod("hasPermission", ApplicationCommand.class, long.class, List.class),
            new InsteadHook(param -> {
                var applicationCommand = (ApplicationCommand) param.args[0];
                var roleIds = (List<Long>) param.args[2];

                if (!(applicationCommand instanceof RemoteApplicationCommand)) {
                    // Allow all builtin commands
                    return true;
                }
                var remoteApplicationCommand = (RemoteApplicationCommand) applicationCommand;

                var channel = storeChannelsSelected.getSelectedChannel();
                var guildId = channel.i();

                if (guildId == 0) {
                    // Allow all commands in DMs
                    return true;
                }

                var channelId = channel.k();
                var applicationId = remoteApplicationCommand.getApplicationId();
                var isUser = this.requestApplicationIndex(new ApplicationIndexSourceUser())
                    .applications
                    .containsKey(applicationId);
                if (isUser) {
                    // Allow all user application commands
                    return true;
                }
                var application = this.requestApplicationIndex(new ApplicationIndexSourceGuild(guildId))
                    .applications
                    .get(applicationId);
                if (application == null) {
                    // Discord requested checking a command from the previous guild - ignore
                    // Some such requests are still processed (if the command exists in both guilds), but it's not an issue as the result doesn't matter for them anyways.
                    return false;
                }
                var user = storeUsers.getMe();
                var memberPermissions = storePermissions.getGuildPermissions()
                    .get(guildId);
                var guild = storeGuilds.getGuild(guildId);

                var applicationPermission = application.permissions_.checkFor(roleIds, channelId, guild, memberPermissions, user, true);
                var commandPermission = remoteApplicationCommand.permissions_.checkFor(roleIds, channelId, guild, memberPermissions, user, applicationPermission);

                return commandPermission;
            })
        );

        Patcher.addPatch(
            StoreApplicationInteractions.class.getDeclaredMethod("handleApplicationCommandResult", MessageResult.class, ApplicationCommandLocalSendData.class, Function0.class, Function1.class),
            new PreHook(param -> {
                var result = (MessageResult) param.args[0];
                var localSendData = (ApplicationCommandLocalSendData) param.args[1];

                if (result instanceof MessageResult.UnknownFailure) {
                    boolean invalidCommandVersion = false;

                    try {
                        var errorResponse = ((MessageResult.UnknownFailure) result)
                            .getError()
                            .getResponse();
                        var error = ReflectUtils.getField(errorResponse, "skemaError");
                        var dataErrors = (List<Error.SkemaErrorItem>) ReflectUtils.getField(
                            ((Map<String, Error.SkemaError>) ReflectUtils.getField(
                                error,
                                "subErrors"
                            ))
                                .get("data"),
                            "errors"
                        );

                        for (var dataError: dataErrors) {
                            var errorCode = (String) ReflectUtils.getField(dataError, "code");
                            if (errorCode.equals("INTERACTION_APPLICATION_COMMAND_INVALID_VERSION")) {
                                ApplicationIndexSource applicationIndexSource = null;
                                var guildId = localSendData.component3();
                                if (guildId != null) {
                                    applicationIndexSource = new ApplicationIndexSourceGuild(guildId);
                                } else {
                                    var channelId = localSendData.component2();
                                    applicationIndexSource = new ApplicationIndexSourceDm(channelId);
                                }
                                this.cleanApplicationIndexCache(applicationIndexSource);

                                var errorMessage = (String) ReflectUtils.getField(dataError, "message");
                                Utils.showToast(errorMessage);

                                break;
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            })
        );

        GatewayAPI.onEvent("GUILD_APPLICATION_COMMAND_INDEX_UPDATE", ApiGuildApplicationCommandIndexUpdate.class, guildApplicationCommandIndexUpdate -> {
            this.cleanApplicationIndexCache(new ApplicationIndexSourceGuild(guildApplicationCommandIndexUpdate.guildId));
            return null;
        });
    }

    private void passCommandData(StoreApplicationCommands storeApplicationCommands, Optional<ApplicationIndexSource> applicationIndexSource, RequestSource requestSource) throws Exception {
        var applicationIndexes = new ArrayList();
        if (applicationIndexSource.isPresent()) {
            applicationIndexes.add(this.requestApplicationIndex(applicationIndexSource.get()));
        }
        applicationIndexes.add(this.requestApplicationIndex(new ApplicationIndexSourceUser()));
        var applicationIndex = new ApplicationIndex(applicationIndexes);

        switch (requestSource) {
            case INITIAL:
                applicationIndex.populateCommandCounts(this.applicationCommandCountField);
                var applications = new ArrayList<Application>(applicationIndex.applications.values());
                Collections.sort(applications, (left, right) -> left.getName().compareTo(right.getName()));
                applications.add(((BuiltInCommandsProvider) ReflectUtils.getField(storeApplicationCommands, "builtInCommandsProvider")).getBuiltInApplication());
                this.handleGuildApplicationsUpdateMethod.invoke(storeApplicationCommands, applications);
                break;

            case BROWSE:
                this.handleDiscoverCommandsUpdateMethod.invoke(storeApplicationCommands, new ArrayList(applicationIndex.applicationCommands.values()));
                break;

            case QUERY:
                this.handleQueryCommandsUpdateMethod.invoke(storeApplicationCommands, new ArrayList(applicationIndex.applicationCommands.values()));
                break;
        }
    }

    private ApplicationIndex requestApplicationIndex(ApplicationIndexSource source) {
        // Reuse application index from cache
        var applicationIndex = source.getFromCache(applicationIndexCache);
        if (!applicationIndex.isPresent()) {
            try {
                // Request application index from API
                applicationIndex = Optional.of(
                    Http.Request.newDiscordRNRequest(source.getEndpoint())
                        .execute()
                        .json(GsonUtils.getGsonRestApi(), ApiApplicationIndex.class)
                        .toModel()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            source.insertIntoCache(applicationIndexCache, applicationIndex.get());
        }
        return applicationIndex.get();
    }

    private void cleanApplicationIndexCache(ApplicationIndexSource source) {
        source.removeFromCache(applicationIndexCache);
    }
}
