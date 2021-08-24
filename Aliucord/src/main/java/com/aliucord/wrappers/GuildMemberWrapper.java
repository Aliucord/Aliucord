/*
 * This file is part of Aliucord, an Android Discord client mod.
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
@SuppressWarnings("unused")
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
        return StaticGuildMemberWrapper.getAvatar(guildMember);
    }

    public final long getGuildId() {
        return StaticGuildMemberWrapper.getGuildId(guildMember);
    }

    public final UtcDateTime getJoinedAt() {
        return StaticGuildMemberWrapper.getJoinedAt(guildMember);
    }

    @Nullable
    public final String getNick() {
        return StaticGuildMemberWrapper.getNick(guildMember);
    }

    public final boolean getIsPending() {
        return StaticGuildMemberWrapper.isPending(guildMember);
    }

    @Nullable
    public final String getPremiumSince() {
        return StaticGuildMemberWrapper.getPremiumSince(guildMember);
    }

    public final List<Long> getRoles() {
        return StaticGuildMemberWrapper.getRoles(guildMember);
    }

    public final User getUser() {
        return StaticGuildMemberWrapper.getUser(guildMember);
    }

    public final Long getUserId() {
        return StaticGuildMemberWrapper.getUserId(guildMember);
    }



    /** @deprecated Use {@link StaticGuildMemberWrapper#getAvatar(GuildMember)} instead */
    @Deprecated
    @Nullable
    public static String getAvatar(GuildMember guildMember) {
        return StaticGuildMemberWrapper.getAvatar(guildMember);
    }

    /** @deprecated Use {@link StaticGuildMemberWrapper#getGuildId(GuildMember)} instead */
    @Deprecated
    public static long getGuildId(GuildMember guildMember) {
        return StaticGuildMemberWrapper.getGuildId(guildMember);
    }

    /** @deprecated Use {@link StaticGuildMemberWrapper#getJoinedAt(GuildMember)} instead */
    @Deprecated
    public static UtcDateTime getJoinedAt(GuildMember guildMember) {
        return StaticGuildMemberWrapper.getJoinedAt(guildMember);
    }

    /** @deprecated Use {@link StaticGuildMemberWrapper#getNick(GuildMember)} instead */
    @Deprecated
    @Nullable
    public static String getNick(GuildMember guildMember) {
        return StaticGuildMemberWrapper.getNick(guildMember);
    }

    /** @deprecated Use {@link StaticGuildMemberWrapper#isPending(GuildMember)} instead */
    @Deprecated
    public static boolean getIsPending(GuildMember guildMember) {
        return StaticGuildMemberWrapper.isPending(guildMember);
    }

    /** @deprecated Use {@link StaticGuildMemberWrapper#getPremiumSince(GuildMember)} instead */
    @Deprecated
    @Nullable
    public static String getPremiumSince(GuildMember guildMember) {
        return StaticGuildMemberWrapper.getPremiumSince(guildMember);
    }

    /** @deprecated Use {@link StaticGuildMemberWrapper#getRoles(GuildMember)} instead */
    @Deprecated
    public static List<Long> getRoles(GuildMember guildMember) {
        return StaticGuildMemberWrapper.getRoles(guildMember);
    }

    /** @deprecated Use {@link StaticGuildMemberWrapper#getUser(GuildMember)} instead */
    @Deprecated
    public static User getUser(GuildMember guildMember) {
        return StaticGuildMemberWrapper.getUser(guildMember);
    }

    /** @deprecated Use {@link StaticGuildMemberWrapper#getUserId(GuildMember)} instead */
    @Deprecated
    public final Long getUserId(GuildMember guildMember) {
        return StaticGuildMemberWrapper.getUserId(guildMember);
    }
}
