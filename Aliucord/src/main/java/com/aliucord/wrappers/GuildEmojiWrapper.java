package com.aliucord.wrappers;

import androidx.annotation.Nullable;

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

    public GuildEmoji raw() {
        return emoji;
    }

    public boolean isAnimated() {
        return emoji.a();
    }

    @Nullable
    public Boolean isAvailable() {
        return emoji.b();
    }

    public long getId() {
        return emoji.c();
    }

    public boolean isManaged() {
        return emoji.d();
    }

    public String getName() {
        return emoji.e();
    }

    public boolean requireColons() {
        return emoji.f();
    }

    public List<Long> getRoles() {
        return emoji.g();
    }
}
