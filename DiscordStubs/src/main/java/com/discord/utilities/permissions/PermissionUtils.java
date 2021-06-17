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