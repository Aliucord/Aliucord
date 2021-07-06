package com.airbnb.lottie.parser;

import android.content.Context;

import com.discord.api.channel.Channel;
import com.discord.models.user.User;

import java.util.Collections;
import java.util.List;

// actually, I have no idea why this class is named AnimatableValueParser and is under lottie package,
// but this class contains a lot of obfuscated methods from other classes
@SuppressWarnings("unused")
public class AnimatableValueParser {
    /** ChannelUtils.getRecipients */
    public static List<User> G0(Channel channel) { return Collections.emptyList(); }
    /** ChannelUtils.isGuildTextyChannel */
    public static boolean k1(Channel channel) { return true; }
    /** ChannelUtils.getDMRecipient */
    public static User v0(Channel channel) { return null; }
    /** ChannelUtils.isTextChannel */
    public static boolean y1(Channel channel) { return true; }
    /** ChannelUtils.getDisplayName */
    public static String y0(Channel channel) { return ""; }
    /** ChannelUtils.getDisplayNameOrDefault */
    public static String z0(Channel channel, Context context, boolean addPrefix) { return ""; }
}
