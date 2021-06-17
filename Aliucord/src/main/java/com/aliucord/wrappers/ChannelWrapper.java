/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

import androidx.annotation.Nullable;

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
        return getApplicationId(channel);
    }

    public final int getBitrate() {
        return getBitrate(channel);
    }

    public final long getGuildId() {
        return getGuildId(channel);
    }

    @Nullable
    public final String getIcon() {
        return getIcon(channel);
    }

    public final long getId() {
        return getId(channel);
    }

    public final long getLastMessageId() {
        return getLastMessageId(channel);
    }

    public final Integer getMessageCount() {
        return getMessageCount(channel);
    }

    public final String getName() {
        return getName(channel);
    }

    public final List<ChannelRecipientNick> getNicks() {
        return getNicks(channel);
    }

    public final boolean isNsfw() {
        return isNsfw(channel);
    }

    public final long getParentId() {
        return getParentId(channel);
    }

    public final int getPosition() {
        return getPosition(channel);
    }

    public final int getRateLimitPerUser() {
        return getRateLimitPerUser(channel);
    }

    public final List<Long> getRecipientIds() {
        return getRecipientIds(channel);
    }

    public final List<User> getRecipients() {
        return getRecipients(channel);
    }

    @Nullable
    public final String getRtcRegion() {
        return getRtcRegion(channel);
    }

    @Nullable
    public final String getTopic() {
        return getTopic(channel);
    }

    public final int getType() {
        return getType(channel);
    }

    public final int getUserLimit() {
        return getUserLimit(channel);
    }



    public static long getApplicationId(Channel channel) {
        return channel.b();
    }

    public static int getBitrate(Channel channel) {
        return channel.c();
    }

    public static long getGuildId(Channel channel) {
        return channel.e();
    }

    @Nullable
    public static String getIcon(Channel channel) {
        return channel.f();
    }

    public static long getId(Channel channel) {
        return channel.g();
    }

    public static long getLastMessageId(Channel channel) {
        return channel.h();
    }

    public static Integer getMessageCount(Channel channel) {
        return channel.k();
    }

    public static String getName(Channel channel) {
        return channel.l();
    }

    public static List<ChannelRecipientNick> getNicks(Channel channel) {
        return channel.m();
    }

    public static boolean isNsfw(Channel channel) {
        return channel.n();
    }

    public static long getParentId(Channel channel) {
        return channel.q();
    }

    public static int getPosition(Channel channel) {
        return channel.s();
    }

    public static int getRateLimitPerUser(Channel channel) {
        return channel.t();
    }

    public static List<Long> getRecipientIds(Channel channel) {
        return channel.u();
    }

    public static List<User> getRecipients(Channel channel) {
        return channel.v();
    }

    @Nullable
    public static String getRtcRegion(Channel channel) {
        return channel.w();
    }

    @Nullable
    public static String getTopic(Channel channel) {
        return channel.y();
    }

    public static int getType(Channel channel) {
        return channel.z();
    }

    public static int getUserLimit(Channel channel) {
        return channel.A();
    }
}
