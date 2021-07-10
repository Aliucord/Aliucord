/*
 * Copyright (c) 2021 Juby210 & Vendicated
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

    public final boolean isDM() {
        return isDM(channel);
    }

    public final boolean isGuild() {
        return isGuild(channel);
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

    public final Integer getDefaultAutoArchiveDuration() {
        return getDefaultAutoArchiveDuration(channel);
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

    public String getMemberListId() {
        return getMemberListId(channel);
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

    public final long getOriginChannelId() {
        return getOriginChannelId(channel);
    }

    public final long getOwnerId() {
        return getOwnerId(channel);
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



    public static boolean isDM(Channel channel) {
        return getGuildId(channel) == 0;
    }

    public static boolean isGuild(Channel channel) {
        return !isDM(channel);
    }

    public static long getApplicationId(Channel channel) {
        return channel.b();
    }

    public static int getBitrate(Channel channel) {
        return channel.c();
    }

    public static Integer getDefaultAutoArchiveDuration(Channel channel) {
        return channel.d();
    }

    public static long getGuildId(Channel channel) {
        return channel.f();
    }

    @Nullable
    public static String getIcon(Channel channel) {
        return channel.g();
    }

    public static long getId(Channel channel) {
        return channel.h();
    }

    public static long getLastMessageId(Channel channel) {
        return channel.i();
    }

    public static String getMemberListId(Channel channel) {
        return channel.k();
    }

    public static Integer getMessageCount(Channel channel) {
        return channel.l();
    }

    public static String getName(Channel channel) {
        return channel.m();
    }

    public static List<ChannelRecipientNick> getNicks(Channel channel) {
        return channel.n();
    }

    public static boolean isNsfw(Channel channel) {
        return channel.o();
    }

    public static long getOriginChannelId(Channel channel) {
        return channel.p();
    }

    public static long getOwnerId(Channel channel) {
        return channel.q();
    }

    public static long getParentId(Channel channel) {
        return channel.r();
    }

    public static int getPosition(Channel channel) {
        return channel.t();
    }

    public static int getRateLimitPerUser(Channel channel) {
        return channel.u();
    }

    public static List<Long> getRecipientIds(Channel channel) {
        return channel.v();
    }

    public static List<User> getRecipients(Channel channel) {
        return channel.w();
    }

    @Nullable
    public static String getRtcRegion(Channel channel) {
        return channel.x();
    }

    @Nullable
    public static String getTopic(Channel channel) {
        return channel.z();
    }

    public static int getType(Channel channel) {
        return channel.A();
    }

    public static int getUserLimit(Channel channel) {
        return channel.B();
    }
}
