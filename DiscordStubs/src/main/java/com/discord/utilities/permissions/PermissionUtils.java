/*
 * Ven's Aliucord Plugins
 * Copyright (C) 2021 Vendicated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.discord.utilities.permissions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.discord.api.channel.Channel;
import com.discord.api.permission.PermissionOverwrite;
import com.discord.api.role.GuildRole;

import java.util.Map;

@SuppressWarnings("unused")
public class PermissionUtils {
    public static final PermissionUtils INSTANCE = new PermissionUtils();
    public final boolean hasAccess(@NonNull Channel channel, @Nullable Long permissions) { return false; }
    public static boolean hasAccess(@NonNull Channel channel, @NonNull Map<Long, Long> permissions) { return false; }
    public final boolean hasAccessWrite(Channel channel, @Nullable Long permissions) { return false; }
    public final boolean hasBypassSlowmodePermissions(@Nullable Long permissions) { return false; }
    public static boolean can(long targetPermission, @Nullable Long permissions) { return false; }
    public final boolean canRole(long targetPermission, @Nullable GuildRole guildRole, @Nullable PermissionOverwrite permissionOverwrite) { return false; }
    public static boolean canEveryone(long targetPermission, @NonNull Channel channel, @Nullable Channel thread, @NonNull Map<Long, GuildRole> guildRoles) { return false; }
    public final boolean canEveryoneRole(long targetPermission, @NonNull Channel channel, @NonNull Map<Long, GuildRole> guildRoles) { return false; }
}