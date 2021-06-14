/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

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

    public final Guild raw() {
        return guild;
    }

    public final Long getAfkChannelId() {
        return guild.b();
    }

    public final int getAfkTimeout() {
        return guild.c();
    }

    public final int getApproximatePresenceCount() {
        return guild.d();
    }

    public final String getBanner() {
        return guild.e();
    }

    public final List<Channel> getChannelUpdates() {
        return guild.f();
    }

    public final List<Channel> getChannels() {
        return guild.g();
    }

    public final Integer getDefaultMessageNotifications() {
        return guild.h();
    }

    public final String getDescription() {
        return guild.i();
    }

    public final List<GuildEmoji> getEmojis() {
        return guild.j();
    }

    public final GuildExplicitContentFilter getExplicitContentFilter() {
        return guild.k();
    }

    public final List<GuildFeature> getFeatures() {
        return guild.l();
    }

    public final String getIcon() {
        return guild.n();
    }

    public final long getId() {
        return guild.o();
    }

    public final String getJoinedAt() {
        return guild.p();
    }

    public final int getApproxMemberCount() {
        return guild.r();
    }

    public final List<GuildMember> getCachedMembers() {
        return guild.s();
    }

    public final int getMfaLevel() {
        return guild.t();
    }

    public final String getName() {
        return guild.u();
    }

    public final boolean isNsfw() {
        return guild.v();
    }

    public final long getOwnerId() {
        return guild.w();
    }

    public final String getPreferredLocale() {
        return guild.x();
    }

    public final int getPremiumSubscriptionCount() {
        return guild.y();
    }

    public final int getPremiumTier() {
        return guild.z();
    }

    public final Long getPublicUpdatesChannelId() {
        return guild.B();
    }

    public final String getRegion() {
        return guild.C();
    }

    public final List<GuildRole> getRoles() {
        return guild.D();
    }

    public final Long getRulesChannelId() {
        return guild.E();
    }

    public final String getSplash() {
        return guild.F();
    }

    public final int getSystemChannelFlags() {
        return guild.H();
    }

    public final Long getSystemChannelId() {
        return guild.I();
    }

    public final List<Channel> getThreads() {
        return guild.J();
    }

    public final boolean isUnavailable() {
        return guild.K();
    }

    public final String getVanityUrlCode() {
        return guild.L();
    }

    public final GuildVerificationLevel getVerificationLevel() {
        return guild.M();
    }
}
