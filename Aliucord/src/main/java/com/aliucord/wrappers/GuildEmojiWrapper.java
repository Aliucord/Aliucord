/*
 * Copyright (c) 2021 Juby210
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
        return emoji.a();
    }

    public final Boolean isAvailable() {
        return emoji.b();
    }

    public final long getId() {
        return emoji.c();
    }

    public final boolean isManaged() {
        return emoji.d();
    }

    public final String getName() {
        return emoji.e();
    }

    public final boolean requireColons() {
        return emoji.f();
    }

    public final List<Long> getRoles() {
        return emoji.g();
    }
}
