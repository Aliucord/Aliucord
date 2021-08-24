/*
 * This file is part of Aliucord, an Android Discord client mod.
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
@SuppressWarnings("unused")
public class ChannelWrapper {
    private final Channel channel;

    public ChannelWrapper(Channel channel) {
        this.channel = channel;
    }

    public final boolean isDM() {
        return StaticChannelWrapper.isDM(channel);
    }

    public final boolean isGuild() {
        return StaticChannelWrapper.isGuild(channel);
    }

    /** Returns the raw (obfuscated) {@link Channel} Object associated with this wrapper */
    public final Channel raw() {
        return channel;
    }

    public final long getApplicationId() {
        return StaticChannelWrapper.getApplicationId(channel);
    }

    public final int getBitrate() {
        return StaticChannelWrapper.getBitrate(channel);
    }

    public final Integer getDefaultAutoArchiveDuration() {
        return StaticChannelWrapper.getDefaultAutoArchiveDuration(channel);
    }

    public final long getGuildId() {
        return StaticChannelWrapper.getGuildId(channel);
    }

    @Nullable
    public final String getIcon() {
        return StaticChannelWrapper.getIcon(channel);
    }

    public final long getId() {
        return StaticChannelWrapper.getId(channel);
    }

    public final long getLastMessageId() {
        return StaticChannelWrapper.getLastMessageId(channel);
    }

    public String getMemberListId() {
        return StaticChannelWrapper.getMemberListId(channel);
    }

    public final Integer getMessageCount() {
        return StaticChannelWrapper.getMessageCount(channel);
    }

    public final String getName() {
        return StaticChannelWrapper.getName(channel);
    }

    public final List<ChannelRecipientNick> getNicks() {
        return StaticChannelWrapper.getNicks(channel);
    }

    public final boolean isNsfw() {
        return StaticChannelWrapper.isNsfw(channel);
    }

    public final long getOriginChannelId() {
        return StaticChannelWrapper.getOriginChannelId(channel);
    }

    public final long getOwnerId() {
        return StaticChannelWrapper.getOwnerId(channel);
    }

    public final long getParentId() {
        return StaticChannelWrapper.getParentId(channel);
    }

    public final int getPosition() {
        return StaticChannelWrapper.getPosition(channel);
    }

    public final int getRateLimitPerUser() {
        return StaticChannelWrapper.getRateLimitPerUser(channel);
    }

    public final List<Long> getRecipientIds() {
        return StaticChannelWrapper.getRecipientIds(channel);
    }

    public final List<User> getRecipients() {
        return StaticChannelWrapper.getRecipients(channel);
    }

    @Nullable
    public final String getRtcRegion() {
        return StaticChannelWrapper.getRtcRegion(channel);
    }

    @Nullable
    public final String getTopic() {
        return StaticChannelWrapper.getTopic(channel);
    }

    public final int getType() {
        return StaticChannelWrapper.getType(channel);
    }

    public final int getUserLimit() {
        return StaticChannelWrapper.getUserLimit(channel);
    }


    /** @deprecated Use {@link StaticChannelWrapper#isDM(Channel)} instead */
    @Deprecated
    public static boolean isDM(Channel channel) {
        return StaticChannelWrapper.isDM(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#isGuild(Channel)} instead */
    @Deprecated
    public static boolean isGuild(Channel channel) {
        return StaticChannelWrapper.isGuild(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getApplicationId(Channel)} instead */
    @Deprecated
    public static long getApplicationId(Channel channel) {
        return StaticChannelWrapper.getApplicationId(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getBitrate(Channel)} instead */
    @Deprecated
    public static int getBitrate(Channel channel) {
        return StaticChannelWrapper.getBitrate(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getDefaultAutoArchiveDuration(Channel)} instead */
    @Deprecated
    public static Integer getDefaultAutoArchiveDuration(Channel channel) {
        return StaticChannelWrapper.getDefaultAutoArchiveDuration(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getGuildId(Channel)} instead */
    @Deprecated
    public static long getGuildId(Channel channel) {
        return StaticChannelWrapper.getGuildId(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getIcon(Channel)} instead */
    @Deprecated
    @Nullable
    public static String getIcon(Channel channel) {
        return StaticChannelWrapper.getIcon(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getId(Channel)} instead */
    @Deprecated
    public static long getId(Channel channel) {
        return StaticChannelWrapper.getId(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getLastMessageId(Channel)} instead */
    @Deprecated
    public static long getLastMessageId(Channel channel) {
        return StaticChannelWrapper.getLastMessageId(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getMemberListId(Channel)} instead */
    @Deprecated
    public static String getMemberListId(Channel channel) {
        return StaticChannelWrapper.getMemberListId(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getMessageCount(Channel)} instead */
    @Deprecated
    public static Integer getMessageCount(Channel channel) {
        return StaticChannelWrapper.getMessageCount(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getName(Channel)} instead */
    @Deprecated
    public static String getName(Channel channel) {
        return StaticChannelWrapper.getName(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getNicks(Channel)} instead */
    @Deprecated
    public static List<ChannelRecipientNick> getNicks(Channel channel) {
        return StaticChannelWrapper.getNicks(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#isNsfw(Channel)} instead */
    @Deprecated
    public static boolean isNsfw(Channel channel) {
        return StaticChannelWrapper.isNsfw(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getOriginChannelId(Channel)} instead */
    @Deprecated
    public static long getOriginChannelId(Channel channel) {
        return StaticChannelWrapper.getOriginChannelId(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getOwnerId(Channel)} instead */
    @Deprecated
    public static long getOwnerId(Channel channel) {
        return StaticChannelWrapper.getOwnerId(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getParentId(Channel)} instead */
    @Deprecated
    public static long getParentId(Channel channel) {
        return StaticChannelWrapper.getParentId(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getPosition(Channel)} instead */
    @Deprecated
    public static int getPosition(Channel channel) {
        return StaticChannelWrapper.getPosition(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getRateLimitPerUser(Channel)} instead */
    @Deprecated
    public static int getRateLimitPerUser(Channel channel) {
        return StaticChannelWrapper.getRateLimitPerUser(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getRecipientIds(Channel)} instead */
    @Deprecated
    public static List<Long> getRecipientIds(Channel channel) {
        return StaticChannelWrapper.getRecipientIds(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getRecipients(Channel)} instead */
    @Deprecated
    public static List<User> getRecipients(Channel channel) {
        return StaticChannelWrapper.getRecipients(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getRtcRegion(Channel)} instead */
    @Deprecated
    @Nullable
    public static String getRtcRegion(Channel channel) {
        return StaticChannelWrapper.getRtcRegion(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getTopic(Channel)} instead */
    @Deprecated
    @Nullable
    public static String getTopic(Channel channel) {
        return StaticChannelWrapper.getTopic(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getType(Channel)} instead */
    @Deprecated
    public static int getType(Channel channel) {
        return StaticChannelWrapper.getType(channel);
    }

    /** @deprecated Use {@link StaticChannelWrapper#getUserLimit(Channel)} instead */
    @Deprecated
    public static int getUserLimit(Channel channel) {
        return StaticChannelWrapper.getUserLimit(channel);
    }
}
