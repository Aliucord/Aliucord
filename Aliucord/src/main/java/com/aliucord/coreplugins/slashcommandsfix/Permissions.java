/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

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

    public Permissions(Optional<Boolean> user, Map<Long, Boolean> roles, Map<Long, Boolean> channels) {
        if (user == null) {
            user = Optional.empty();
        }
        if (roles == null) {
            roles = new HashMap<>();
        }
        if (channels == null) {
            channels = new HashMap<>();
        }
        this.user = user;
        this.roles = roles;
        this.channels = channels;
    }

    public boolean checkFor(List<Long> roleIds, long channelId, Guild guild, long memberPermissions, MeUser user) {
        var guildId = guild.component7();
        var defaultChannelPermission = this.channels.getOrDefault(guildId - 1, true);
        var channelPermission = Optional.ofNullable(this.channels.get(channelId))
            .orElse(defaultChannelPermission);
        var defaultRolePermission = this.roles.getOrDefault(guildId, true);
        var rolePermission = this.calculateRolePermission(roleIds, defaultRolePermission);
        var userPermission = this.user.orElse(true);
        var administratorPermissions = PermissionUtils.canAndIsElevated(Permission.ADMINISTRATOR, memberPermissions, user.getMfaEnabled(), guild.getMfaLevel());

        return administratorPermissions || (channelPermission && (userPermission || rolePermission));
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
