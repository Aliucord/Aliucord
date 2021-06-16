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
    private final EmbedThumbnail image;

    public ThumbnailWrapper(EmbedThumbnail image) {
        this.image = image;
    }

    /** Returns the raw (obfuscated) {@link EmbedThumbnail} Object associated with this wrapper */
    public final EmbedThumbnail raw() {
        return image;
    }

    public final String getUrl() {
        return image.c();
    }

    public final String getProxyUrl() {
        return image.b();
    }

    @Nullable
    public final Integer getWidth() {
        return image.d();
    }

    @Nullable
    public final Integer getHeight() {
        return image.a();
    }
}
