package com.discord.utilities.guilds;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.discord.api.role.GuildRole;
import com.discord.models.member.GuildMember;

import java.util.Collection;
import java.util.Map;

public final class RoleUtils {
    // public static final RoleUtils INSTANCE = new RoleUtils(); // This is useless as everything is static

    public static boolean containsRoleMentions(String str) { return false; }

    @Nullable
    public static GuildRole getHighestHoistedRole(@NonNull Map<Long, GuildRole> roles, @Nullable GuildMember guildMember) { return null; }

    @Nullable
    public static GuildRole getHighestRole(@NonNull Map<Long, GuildRole> roles, @Nullable GuildMember guildMember) { return null; }

    public static int getOpaqueColor(GuildRole guildRole, int i) { return 0; }

    // public static Comparator<GuildRole> getROLE_COMPARATOR() { }

    public static int getRoleColor(@NonNull GuildRole guildRole, @NonNull Context context) { return 0; }

    public static int getRoleColor(@NonNull GuildRole guildRole, @NonNull Context context, @Nullable Integer defaultColor) { return 0; }

    public static boolean isDefaultColor(@NonNull GuildRole guildRole) { return false; }

    public static boolean rankEquals(@NonNull GuildRole guildRole, @NonNull GuildRole guildRole2) { return false; }

    public static boolean rankIsHigher(@NonNull GuildRole guildRole, @NonNull GuildRole guildRole2) { return false; }

    /** Returns null if memberRoleIds is null or empty */
    @Nullable
    public static GuildRole getHighestRole(@NonNull Map<Long, GuildRole> roles, @Nullable Collection<Long> memberRoleIds) { return null; }

    public static int getOpaqueColor(@NonNull GuildRole guildRole) { return 0; }
}
