/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

import androidx.annotation.Nullable;

import com.discord.api.channel.Channel;
import com.discord.api.emoji.GuildEmoji;
import com.discord.api.guild.*;
import com.discord.api.guildmember.GuildMember;
import com.discord.api.role.GuildRole;

import java.util.List;

/**
 * Wraps the obfuscated {@link Guild} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class GuildWrapper {
    private final Guild guild;

    public GuildWrapper(Guild guild) {
        this.guild = guild;
    }

    /** Returns the raw (obfuscated) {@link Guild} Object associated with this wrapper */
    public final Guild raw() {
        return guild;
    }

    @Nullable
    public final Long getAfkChannelId() {
        return getAfkChannelId(guild);
    }

    public final int getAfkTimeout() {
        return getAfkTimeout(guild);
    }

    public final int getApproximatePresenceCount() {
        return getApproximatePresenceCount(guild);
    }

    @Nullable
    public final String getBanner() {
        return getBanner(guild);
    }

    public final List<Channel> getChannelUpdates() {
        return getChannelUpdates(guild);
    }

    public final List<Channel> getChannels() {
        return getChannels(guild);
    }

    public final Integer getDefaultMessageNotifications() {
        return getDefaultMessageNotifications(guild);
    }

    @Nullable
    public final String getDescription() {
        return getDescription(guild);
    }

    public final List<GuildEmoji> getEmojis() {
        return getEmojis(guild);
    }

    public final GuildExplicitContentFilter getExplicitContentFilter() {
        return getExplicitContentFilter(guild);
    }

    public final List<GuildFeature> getFeatures() {
        return getFeatures(guild);
    }

    @Nullable
    public final String getIcon() {
        return getIcon(guild);
    }

    public final long getId() {
        return getId(guild);
    }

    @Nullable
    public final String getJoinedAt() {
        return getJoinedAt(guild);
    }

    public final int getApproxMemberCount() {
        return getApproxMemberCount(guild);
    }

    public final List<GuildMember> getCachedMembers() {
        return getCachedMembers(guild);
    }

    public final int getMfaLevel() {
        return getMfaLevel(guild);
    }

    public final String getName() {
        return getName(guild);
    }

    public final boolean isNsfw() {
        return isNsfw(guild);
    }

    public final long getOwnerId() {
        return getOwnerId(guild);
    }

    @Nullable
    public final String getPreferredLocale() {
        return getPreferredLocale(guild);
    }

    public final int getPremiumSubscriptionCount() {
        return getPremiumSubscriptionCount(guild);
    }

    public final int getPremiumTier() {
        return getPremiumTier(guild);
    }

    @Nullable
    public final Long getPublicUpdatesChannelId() {
        return getPublicUpdatesChannelId(guild);
    }

    public final String getRegion() {
        return getRegion(guild);
    }

    public final List<GuildRole> getRoles() {
        return getRoles(guild);
    }

    @Nullable
    public final Long getRulesChannelId() {
        return getRulesChannelId(guild);
    }

    @Nullable
    public final String getSplash() {
        return getSplash(guild);
    }

    public final int getSystemChannelFlags() {
        return getSystemChannelFlags(guild);
    }

    @Nullable
    public final Long getSystemChannelId() {
        return getSystemChannelId(guild);
    }

    public final List<Channel> getThreads() {
        return getThreads(guild);
    }

    public final boolean isUnavailable() {
        return isUnavailable(guild);
    }

    @Nullable
    public final String getVanityUrlCode() {
        return getVanityUrlCode(guild);
    }

    public final GuildVerificationLevel getVerificationLevel() {
        return getVerificationLevel(guild);
    }




    @Nullable
    public static Long getAfkChannelId(Guild guild) {
        return guild.b();
    }

    public static int getAfkTimeout(Guild guild) {
        return guild.c();
    }

    public static int getApproximatePresenceCount(Guild guild) {
        return guild.d();
    }

    @Nullable
    public static String getBanner(Guild guild) {
        return guild.e();
    }

    public static List<Channel> getChannelUpdates(Guild guild) {
        return guild.f();
    }

    public static List<Channel> getChannels(Guild guild) {
        return guild.g();
    }

    public static Integer getDefaultMessageNotifications(Guild guild) {
        return guild.h();
    }

    @Nullable
    public static String getDescription(Guild guild) {
        return guild.i();
    }

    public static List<GuildEmoji> getEmojis(Guild guild) {
        return guild.j();
    }

    public static GuildExplicitContentFilter getExplicitContentFilter(Guild guild) {
        return guild.k();
    }

    public static List<GuildFeature> getFeatures(Guild guild) {
        return guild.l();
    }

    @Nullable
    public static String getIcon(Guild guild) {
        return guild.n();
    }

    public static long getId(Guild guild) {
        return guild.o();
    }

    @Nullable
    public static String getJoinedAt(Guild guild) {
        return guild.p();
    }

    public static int getApproxMemberCount(Guild guild) {
        return guild.r();
    }

    public static List<GuildMember> getCachedMembers(Guild guild) {
        return guild.s();
    }

    public static int getMfaLevel(Guild guild) {
        return guild.t();
    }

    public static String getName(Guild guild) {
        return guild.u();
    }

    public static boolean isNsfw(Guild guild) {
        return guild.v();
    }

    public static long getOwnerId(Guild guild) {
        return guild.w();
    }

    @Nullable
    public static String getPreferredLocale(Guild guild) {
        return guild.x();
    }

    public static int getPremiumSubscriptionCount(Guild guild) {
        return guild.y();
    }

    public static int getPremiumTier(Guild guild) {
        return guild.z();
    }

    @Nullable
    public static Long getPublicUpdatesChannelId(Guild guild) {
        return guild.B();
    }

    public static String getRegion(Guild guild) {
        return guild.C();
    }

    public static List<GuildRole> getRoles(Guild guild) {
        return guild.D();
    }

    @Nullable
    public static Long getRulesChannelId(Guild guild) {
        return guild.E();
    }

    @Nullable
    public static String getSplash(Guild guild) {
        return guild.F();
    }

    public static int getSystemChannelFlags(Guild guild) {
        return guild.H();
    }

    @Nullable
    public static Long getSystemChannelId(Guild guild) {
        return guild.I();
    }

    public static List<Channel> getThreads(Guild guild) {
        return guild.J();
    }

    public static boolean isUnavailable(Guild guild) {
        return guild.K();
    }

    @Nullable
    public static String getVanityUrlCode(Guild guild) {
        return guild.L();
    }

    public static GuildVerificationLevel getVerificationLevel(Guild guild) {
        return guild.M();
    }
}
