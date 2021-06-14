/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

import com.discord.api.channel.Channel;
import com.discord.api.channel.ChannelRecipientNick;
import com.discord.api.user.User;

import java.util.List;

/**
 * Wraps the obfuscated {@link Channel} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class ChannelWrapper {
    private final Channel channel;

    public ChannelWrapper(Channel channel) {
        this.channel = channel;
    }

    /** Returns the raw (obfuscated) {@link Channel} Object associated with this wrapper */
    public final Channel raw() {
        return channel;
    }

    public final long getApplicationId() {
        return channel.b();
    }

    public final int getBitrate() {
        return channel.c();
    }

    public final long getGuildId() {
        return channel.e();
    }

    public final String getIcon() {
        return channel.f();
    }

    public final long getId() {
        return channel.g();
    }

    public final long getLastMessageId() {
        return channel.h();
    }

    public final Integer getMessageCount() {
        return channel.k();
    }

    public final String getName() {
        return channel.l();
    }

    public final List<ChannelRecipientNick> getNicks() {
        return channel.m();
    }

    public final boolean isNsfw() {
        return channel.n();
    }

    public final long getParentId() {
        return channel.q();
    }

    public final int getPosition() {
        return channel.s();
    }

    public final int getRateLimitPerUser() {
        return channel.t();
    }

    public final List<Long> getRecipientIds() {
        return channel.u();
    }

    public final List<User> getRecipients() {
        return channel.v();
    }

    public final String getRtcRegion() {
        return channel.w();
    }

    public final String getTopic() {
        return channel.y();
    }

    public final int getType() {
        return channel.z();
    }

    public final int getUserLimit() {
        return channel.A();
    }
}
