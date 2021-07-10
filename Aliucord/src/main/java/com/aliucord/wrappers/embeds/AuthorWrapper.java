/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds;

import androidx.annotation.Nullable;

import com.discord.api.message.embed.EmbedAuthor;

/**
 * Wraps the obfuscated {@link EmbedAuthor} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class AuthorWrapper {
    private final EmbedAuthor author;

    public AuthorWrapper(EmbedAuthor author) {
        this.author = author;
    }

    /** Returns the raw (obfuscated) {@link EmbedAuthor} Object associated with this wrapper */
    public final EmbedAuthor raw() {
        return author;
    }

    public final String getName() {
        return getName(author);
    }

    @Nullable
    public final String getProxyIconUrl() {
        return getProxyIconUrl(author);
    }

    @Nullable
    public final String getUrl() {
        return getUrl(author);
    }


    public static String getName(EmbedAuthor author) {
        return author.a();
    }

    @Nullable
    public static String getProxyIconUrl(EmbedAuthor author) {
        return author.b();
    }

    @Nullable
    public static String getUrl(EmbedAuthor author) {
        return author.c();
    }
}
