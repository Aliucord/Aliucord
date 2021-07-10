/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds;

import androidx.annotation.Nullable;

import com.discord.api.message.embed.EmbedVideo;

/**
 * Wraps the obfuscated {@link EmbedVideo} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class VideoWrapper {
    private final EmbedVideo video;

    public VideoWrapper(EmbedVideo video) {
        this.video = video;
    }

    /** Returns the raw (obfuscated) {@link EmbedVideo} Object associated with this wrapper */
    public final EmbedVideo raw() {
        return video;
    }

    public final String getUrl() {
        return getUrl(video);
    }

    public final String getProxyUrl() {
        return getProxyUrl(video);
    }

    @Nullable
    public final Integer getHeight() {
        return getHeight(video);
    }

    @Nullable
    public final Integer getWidth() {
        return getWidth(video);
    }



    public static String getUrl(EmbedVideo video) {
        return video.c();
    }

    public static String getProxyUrl(EmbedVideo video) {
        return video.b();
    }

    @Nullable
    public static Integer getHeight(EmbedVideo video) {
        return video.a();
    }

    @Nullable
    public static Integer getWidth(EmbedVideo video) {
        return video.d();
    }
}
