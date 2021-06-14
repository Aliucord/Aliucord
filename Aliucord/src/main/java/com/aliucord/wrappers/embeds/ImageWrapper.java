package com.aliucord.wrappers.embeds;

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
