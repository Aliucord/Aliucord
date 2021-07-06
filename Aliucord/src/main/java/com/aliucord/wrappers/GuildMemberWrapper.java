/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

import androidx.annotation.Nullable;

import com.discord.api.guildmember.GuildMember;
import com.discord.api.user.User;
import com.discord.api.utcdatetime.UtcDateTime;

import java.util.List;

/**
 * Wraps the obfuscated {@link GuildMember} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class GuildMemberWrapper {
    private final GuildMember guildMember;

    public GuildMemberWrapper(GuildMember guildMember) {
        this.guildMember = guildMember;
    }

    /** Returns the raw (obfuscated) {@link GuildMember} Object associated with this wrapper */
    public final GuildMember raw() {
        return guildMember;
    }

    @Nullable
    public final String getAvatar() {
        return getAvatar(guildMember);
    }

    public final long getGuildId() {
        return getGuildId(guildMember);
    }

    public final UtcDateTime getJoinedAt() {
        return getJoinedAt(guildMember);
    }

    @Nullable
    public final String getNick() {
        return getNick(guildMember);
    }

    public final boolean getIsPending() {
        return getIsPending(guildMember);
    }

    @Nullable
    public final String getPremiumSince() {
        return getPremiumSince(guildMember);
    }

    public final List<Long> getRoles() {
        return getRoles(guildMember);
    }

    public final User getUser() {
        return getUser(guildMember);
    }

    public final Long getUserId() {
        return getUserId(guildMember);
    }



    @Nullable
    public static String getAvatar(GuildMember guildMember) {
        return guildMember.b();
    }

    public static long getGuildId(GuildMember guildMember) {
        return guildMember.c();
    }

    public static UtcDateTime getJoinedAt(GuildMember guildMember) {
        return guildMember.d();
    }

    @Nullable
    public static String getNick(GuildMember guildMember) {
        return guildMember.e();
    }

    public static boolean getIsPending(GuildMember guildMember) {
        return guildMember.f();
    }

    @Nullable
    public static String getPremiumSince(GuildMember guildMember) {
        return guildMember.g();
    }

    public static List<Long> getRoles(GuildMember guildMember) {
        return guildMember.i();
    }

    public static User getUser(GuildMember guildMember) {
        return guildMember.j();
    }

    public final Long getUserId(GuildMember guildMember) {
        return guildMember.k();
    }
}
