/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds;

import androidx.annotation.Nullable;

import com.discord.api.message.embed.EmbedThumbnail;

/**
 * Wraps the obfuscated {@link EmbedThumbnail} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class ThumbnailWrapper {
    private final EmbedThumbnail thumbnail;

    public ThumbnailWrapper(EmbedThumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    /** Returns the raw (obfuscated) {@link EmbedThumbnail} Object associated with this wrapper */
    public final EmbedThumbnail raw() {
        return thumbnail;
    }

    public final String getUrl() {
        return getUrl(thumbnail);
    }

    public final String getProxyUrl() {
        return getProxyUrl(thumbnail);
    }

    @Nullable
    public final Integer getWidth() {
        return getWidth(thumbnail);
    }

    @Nullable
    public final Integer getHeight() {
        return getHeight(thumbnail);
    }


    public static String getUrl(EmbedThumbnail thumbnail) {
        return thumbnail.c();
    }

    public static String getProxyUrl(EmbedThumbnail thumbnail) {
        return thumbnail.b();
    }

    @Nullable
    public static Integer getWidth(EmbedThumbnail thumbnail) {
        return thumbnail.d();
    }

    @Nullable
    public static Integer getHeight(EmbedThumbnail thumbnail) {
        return thumbnail.a();
    }
}
