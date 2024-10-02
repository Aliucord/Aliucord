/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import com.discord.api.channel.Channel;
import com.discord.api.permission.Permission;
import com.discord.models.guild.Guild;
import com.discord.models.user.MeUser;
import com.discord.utilities.permissions.PermissionUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class Permissions {
    public Optional<Boolean> user;
    public Map<Long, Boolean> roles;
    public Map<Long, Boolean> channels;
    public Optional<Long> defaultMemberPermissions;

    public Permissions(Optional<Boolean> user, Map<Long, Boolean> roles, Map<Long, Boolean> channels, Optional<Long> defaultMemberPermissions) {
        this.user = Optional.ofNullable(user).orElse(Optional.empty());
        this.roles = Optional.ofNullable(roles).orElse(new HashMap<>());
        this.channels = Optional.ofNullable(channels).orElse(new HashMap<>());
        this.defaultMemberPermissions = Optional.ofNullable(defaultMemberPermissions).orElse(Optional.empty());
    }

    public boolean checkFor(List<Long> roleIds, Channel channel, Guild guild, long memberPermissions, MeUser user, boolean defaultPermission) {
        var guildId = guild.component7();
        var defaultChannelPermissionId = guildId - 1;
        var defaultChannelPermission = this.channels.getOrDefault(defaultChannelPermissionId, defaultPermission);
        var channelType = channel.D();
        var channelId = channel.k();
        var permissionChannelId = channelId;
        // Threads inherit permissions from their parent channels
        if (channelType == Channel.ANNOUNCEMENT_THREAD || channelType == Channel.PUBLIC_THREAD || channelType == Channel.PRIVATE_THREAD) {
            var channelParentId = channel.u();
            permissionChannelId = channelParentId;
        }
        var channelPermission = Optional.ofNullable(this.channels.get(permissionChannelId))
            .orElse(defaultChannelPermission);
        var defaultMemberPermission = this.defaultMemberPermissions
            .map(
                defaultMemberPermissions -> defaultMemberPermissions != 0
                    && PermissionUtils.canAndIsElevated(
                        defaultMemberPermissions,
                        memberPermissions,
                        user.getMfaEnabled(),
                        guild.getMfaLevel()
                    )
            )
            .orElse(defaultPermission);
        var everyoneRoleId = guildId;
        var defaultRolePermission = this.roles.getOrDefault(everyoneRoleId, defaultMemberPermission);
        var rolePermission = this.calculateRolePermission(roleIds, defaultRolePermission);
        var userPermission = this.user.orElse(defaultMemberPermission);
        var administratorPermission = PermissionUtils.canAndIsElevated(
            Permission.ADMINISTRATOR,
            memberPermissions,
            user.getMfaEnabled(),
            guild.getMfaLevel()
        );

        return administratorPermission || (channelPermission && (userPermission || rolePermission));
    }

    private boolean calculateRolePermission(List<Long> roleIds, boolean defaultPermission) {
        var calculatedRolePermission = defaultPermission;
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
