/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds;

import androidx.annotation.Nullable;

import com.discord.api.message.embed.EmbedImage;

/**
 * Wraps the obfuscated {@link EmbedImage} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class ImageWrapper {
    private final EmbedImage image;

    public ImageWrapper(EmbedImage image) {
        this.image = image;
    }

    /** Returns the raw (obfuscated) {@link EmbedImage} Object associated with this wrapper */
    public final EmbedImage raw() {
        return image;
    }

    public final String getUrl() {
        return getUrl(image);
    }

    public final String getProxyUrl() {
        return getProxyUrl(image);
    }

    @Nullable
    public final Integer getHeight() {
        return getHeight(image);
    }

    @Nullable
    public final Integer getWidth() {
        return getWidth(image);
    }

    public static String getUrl(EmbedImage image) {
        return image.c();
    }

    public static String getProxyUrl(EmbedImage image) {
        return image.b();
    }

    @Nullable
    public static Integer getHeight(EmbedImage image) {
        return image.a();
    }

    @Nullable
    public static Integer getWidth(EmbedImage image) {
        return image.d();
    }
}
