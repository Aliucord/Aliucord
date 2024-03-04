/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import android.content.Context;
import com.aliucord.Http;
import com.aliucord.patcher.InsteadHook;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PreHook;
import com.aliucord.utils.GsonUtils;
import com.aliucord.utils.ReflectUtils;
import com.aliucord.Logger;
import com.discord.models.commands.ApplicationCommand;
import com.discord.models.commands.ApplicationCommandKt;
import com.discord.models.commands.ApplicationCommandOption;
import com.discord.stores.BuiltInCommandsProvider;
import com.discord.stores.StoreApplicationCommands;
import com.discord.stores.StoreApplicationCommands$requestApplicationCommands$1;
import com.discord.stores.StoreApplicationCommands$requestApplicationCommandsQuery$1;
import com.discord.stores.StoreApplicationCommands$requestApplications$1;
import com.discord.stores.StoreApplicationCommandsKt;
import com.discord.stores.StoreStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class Patches {
    private class ApiApplication {
        public final long id;
        public final String name;
        public final String icon;
        public final ApiPermissions permissions;

        public ApiApplication() {
            this.id = 0;
            this.name = null;
            this.icon = null;
            this.permissions = null;
        }

        public Application toModel(int commandCount) {
            Permissions permissions = null;
            if (this.permissions != null) {
                permissions = this.permissions.toModel();
            }
            return new Application(this.id, this.name, this.icon, permissions, commandCount);
        }
    }

    private class ApiApplicationCommand {
        public final long id;
        public final long applicationId;
        public final String name;
        public final String description;
        public final List<com.discord.api.commands.ApplicationCommandOption> options;
        public final ApiPermissions permissions;
        public final String version;

        public ApiApplicationCommand() {
            this.id = 0;
            this.applicationId = 0;
            this.name = null;
            this.description = null;
            this.options = null;
            this.permissions = null;
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
            Permissions permissions = null;
            if (this.permissions != null) {
                permissions = this.permissions.toModel();
            }
            return new RemoteApplicationCommand(String.valueOf(this.id), this.applicationId, this.name, this.description, options, permissions, this.version);
        }
    }

    private class ApiPermissions {
        public Boolean user;
        public Map<Long, Boolean> roles;
        public Map<Long, Boolean> channels;

        public ApiPermissions() {
            this.user = null;
            this.roles = null;
            this.channels = null;
        }

        public Permissions toModel() {
            return new Permissions(user, roles, channels);
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

    private class Permissions {
        public Boolean user;
        public Map<Long, Boolean> roles;
        public Map<Long, Boolean> channels;

        public Permissions(Boolean user, Map<Long, Boolean> roles, Map<Long, Boolean> channels) {
            this.user = user;
            this.roles = roles;
            this.channels = channels;
        }

        public boolean checkFor(List<Long> roleIds, long channelId, long guildId) {
            var channelPermission = this.channels.get(channelId);
            var defaultChannelPermission = this.channels.getOrDefault(guildId - 1, true);
            var userPermission = this.user;
            var rolePermission = this.calculateRolePermission(roleIds);
            var defaultRolePermission = this.roles.getOrDefault(guildId, true);
            return ((channelPermission != null && channelPermission) || (channelPermission == null && defaultChannelPermission)) &&
                ((userPermission != null && userPermission) ||
                    (rolePermission != null && rolePermission) ||
                    (rolePermission == null && defaultRolePermission));
        }

        private Boolean calculateRolePermission(List<Long> roleIds) {
            Boolean calculatedRolePermission = null;
            for (var roleId: roleIds) {
                var rolePermission = this.roles.get(roleId);
                if (rolePermission != null) {
                    calculatedRolePermission = rolePermission;
                    if (rolePermission) {
                        break;
                    }
                }
            }
            return calculatedRolePermission;
        }
    }

    private class Application extends com.discord.models.commands.Application {
        public Permissions permissions_;

        public Application(long id, String name, String icon, Permissions permissions, int commandCount) {
            super(id, name, icon, null, commandCount, null, false);
            this.permissions_ = permissions;
        }
    }

    private class RemoteApplicationCommand extends com.discord.models.commands.RemoteApplicationCommand {
        public Permissions permissions_;

        public RemoteApplicationCommand(String id, long applicationId, String name, String description, List<ApplicationCommandOption> options, Permissions permissions, String version) {
            super(id, applicationId, name, description, options, null, version, true, null, null); // TODO: defaultPermissions
            this.permissions_ = permissions;
        }
    }

    private enum RequestSource {
        GUILD,
        BROWSE,
        QUERY;
    }

    private Map<Long, ApplicationIndex> guildApplicationIndexes;
    Logger logger;

    Patches(Logger logger) {
        this.guildApplicationIndexes = new HashMap();
        this.logger = logger;
    }

    public void loadPatches(Context context) throws Throwable {
        var storeApplicationCommands = StoreStream.getApplicationCommands();
        var storeChannelsSelected = StoreStream.getChannelsSelected();

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

        // Command permission check
        Patcher.addPatch(
            ApplicationCommandKt.class.getDeclaredMethod("hasPermission", ApplicationCommand.class, long.class, List.class),
            new InsteadHook(param -> {
                var applicationCommand = (ApplicationCommand) param.args[0];
                var userId = (long) param.args[1];
                var roles = (List<Long>) param.args[2];

                var selectedChannel = storeChannelsSelected.getSelectedChannel();
                var channelId = selectedChannel.k();
                var guildId = selectedChannel.i();
                var applicationId = applicationCommand.getApplicationId();
                this.logger.debug(String.format("Checking permissions for command %s (%s): user %d with %d roles, channel %d, guild %d", applicationCommand.getId(), applicationCommand.getName(), userId, roles.size(), channelId, guildId));
                var optionalApplication = this.requestApplicationIndex(guildId)
                    .applications
                    .stream()
                    .filter(a -> {
                        var id = a.component1();
                        return id == applicationId;
                    })
                    .findFirst();
                return !(applicationCommand instanceof RemoteApplicationCommand)
                    || (optionalApplication.get().permissions_.checkFor(roles, channelId, guildId)
                        && ((RemoteApplicationCommand) applicationCommand).permissions_.checkFor(roles, channelId, guildId));
            })
        );
    }

    // Upcasting Object generates a warning and we need that to get private fields with reflection
    @SuppressWarnings("unchecked")
    private void passCommandData(StoreApplicationCommands storeApplicationCommands, long guildId, RequestSource requestSource) {
        // TODO: Cache the fields as they are requested every time this runs

        var applicationIndex = this.requestApplicationIndex(guildId);

        // Pass the information to StoreApplicationCommands
        if (requestSource == RequestSource.GUILD) {
            try {
                var applications = new ArrayList(applicationIndex.applications);
                Collections.sort(applications, new Comparator<Application>() {
                    @Override
                    public int compare(Application left, Application right) {
                        return left.getName().compareTo(right.getName());
                    }
                });
                applications.add(((BuiltInCommandsProvider) ReflectUtils.getField(storeApplicationCommands, "builtInCommandsProvider")).getBuiltInApplication());
                var handleGuildApplicationsUpdateMethod = StoreApplicationCommands.class.getDeclaredMethod("handleGuildApplicationsUpdate", List.class);
                handleGuildApplicationsUpdateMethod.setAccessible(true);
                handleGuildApplicationsUpdateMethod.invoke(storeApplicationCommands, applications);
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

    private ApplicationIndex requestApplicationIndex(long guildId) {
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
        return applicationIndex;
    }
}
