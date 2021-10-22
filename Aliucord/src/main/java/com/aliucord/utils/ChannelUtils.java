/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import android.content.Context;

import com.discord.api.channel.Channel;
import com.discord.models.user.User;

import java.util.List;

@SuppressWarnings("unused")
public final class ChannelUtils {
    public static List<User> getRecipients(Channel channel) {
        return com.discord.api.channel.ChannelUtils.g(channel);
    }

    public static boolean isGuildTextChannel(Channel channel) {
        return com.discord.api.channel.ChannelUtils.q(channel);
    }

    public static User getDMRecipient(Channel channel) {
        return com.discord.api.channel.ChannelUtils.a(channel);
    }

    public static boolean isTextChannel(Channel channel) {
        return com.discord.api.channel.ChannelUtils.B(channel);
    }

    public static String getDisplayName(Channel channel) {
        return com.discord.api.channel.ChannelUtils.c(channel);
    }

    public static String getDisplayNameOrDefault(Channel channel, Context context, boolean addPrefix) {
        return com.discord.api.channel.ChannelUtils.d(channel, context, addPrefix);
    }
}
