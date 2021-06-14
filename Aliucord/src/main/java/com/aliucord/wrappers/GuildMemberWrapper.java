/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

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

    public final String getAvatar() {
        return guildMember.b();
    }

    public final long getGuildId() {
        return guildMember.c();
    }

    public final UtcDateTime getJoinedAt() {
        return guildMember.d();
    }

    public final String getNick() {
        return guildMember.e();
    }

    public final boolean getIsPending() {
        return guildMember.f();
    }

    public final String getPremiumSince() {
        return guildMember.g();
    }

    public final List<Long> getRoles() {
        return guildMember.i();
    }

    public final User getUser() {
        return guildMember.j();
    }

    public final Long getUserId() {
        return guildMember.k();
    }
}
