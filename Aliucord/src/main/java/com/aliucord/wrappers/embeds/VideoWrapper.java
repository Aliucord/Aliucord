package com.aliucord.wrappers.embeds;

import com.discord.api.message.embed.EmbedVideo;

/**
 * Wraps the obfuscated {@link EmbedVideo} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class VideoWrapper {
    private final EmbedVideo image;

    public VideoWrapper(EmbedVideo image) {
        this.image = image;
    }

    /** Returns the raw (obfuscated) {@link EmbedVideo} Object associated with this wrapper */
    public final EmbedVideo raw() {
        return image;
    }

    public final String getUrl() {
        return image.c();
    }

    public final String getProxyUrl() {
        return image.b();
    }

    public final Integer getHeight() {
        return image.a();
    }

    public final Integer getWidth() {
        return image.d();
    }
}
