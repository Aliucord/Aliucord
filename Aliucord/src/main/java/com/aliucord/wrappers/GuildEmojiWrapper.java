/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

import com.discord.api.emoji.GuildEmoji;

import java.util.List;

/**
 * Wraps the obfuscated {@link GuildEmoji} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class GuildEmojiWrapper {
    private final GuildEmoji emoji;

    public GuildEmojiWrapper(GuildEmoji emoji) {
        this.emoji = emoji;
    }

    /** Returns the raw (obfuscated) {@link GuildEmoji} Object associated with this wrapper */
    public final GuildEmoji raw() {
        return emoji;
    }

    public final boolean isAnimated() {
        return isAnimated(emoji);
    }

    public final Boolean isAvailable() {
        return isAvailable(emoji);
    }

    public final long getId() {
        return getId(emoji);
    }

    public final boolean isManaged() {
        return isManaged(emoji);
    }

    public final String getName() {
        return getName(emoji);
    }

    public final boolean requireColons() {
        return requireColons(emoji);
    }

    public final List<Long> getRoles() {
        return getRoles(emoji);
    }



    public static boolean isAnimated(GuildEmoji emoji) {
        return emoji.a();
    }

    public static Boolean isAvailable(GuildEmoji emoji) {
        return emoji.b();
    }

    public static long getId(GuildEmoji emoji) {
        return emoji.c();
    }

    public static boolean isManaged(GuildEmoji emoji) {
        return emoji.d();
    }

    public static String getName(GuildEmoji emoji) {
        return emoji.e();
    }

    public static boolean requireColons(GuildEmoji emoji) {
        return emoji.f();
    }

    public static List<Long> getRoles(GuildEmoji emoji) {
        return emoji.g();
    }
}
