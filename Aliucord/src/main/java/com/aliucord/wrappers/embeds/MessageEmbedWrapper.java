/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds;

import com.aliucord.CollectionUtils;
import com.discord.api.message.embed.EmbedAuthor;
import com.discord.api.message.embed.EmbedField;
import com.discord.api.message.embed.EmbedFooter;
import com.discord.api.message.embed.EmbedImage;
import com.discord.api.message.embed.EmbedProvider;
import com.discord.api.message.embed.EmbedThumbnail;
import com.discord.api.message.embed.EmbedType;
import com.discord.api.message.embed.EmbedVideo;
import com.discord.api.message.embed.MessageEmbed;
import com.discord.api.utcdatetime.UtcDateTime;

import java.util.List;

/**
 * Wraps the obfuscated {@link MessageEmbed} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class MessageEmbedWrapper {
    private final MessageEmbed embed;

    public MessageEmbedWrapper(MessageEmbed embed) {
        this.embed = embed;
    }

    /** Returns the raw (obfuscated) {@link MessageEmbed} Object associated with this wrapper */
    public final MessageEmbed raw() {
        return embed;
    }

    /** Returns the raw (obfuscated) {@link EmbedAuthor} Object associated with this wrapper */
    public final EmbedAuthor getRawAuthor() {
        return embed.a();
    }

    public final AuthorWrapper getAuthor() {
        return new AuthorWrapper(getRawAuthor());
    }

    public final Integer getColor() {
        return embed.b();
    }

    public final String getDescription() {
        return embed.c();
    }

    /** Returns the raw (obfuscated) {@link EmbedField}s associated with this wrapper */
    public final List<EmbedField> getRawFields() {
        return embed.d();
    }

    public final List<FieldWrapper> getFields() {
        return CollectionUtils.map(getRawFields(), FieldWrapper::new);
    }

    /** Returns the raw (obfuscated) {@link EmbedFooter} Object associated with this wrapper */
    public final EmbedFooter getRawFooter() {
        return embed.e();
    }

    public final FooterWrapper getFooter() {
        return new FooterWrapper(getRawFooter());
    }

    /** Returns the raw (obfuscated) {@link EmbedThumbnail} Object associated with this wrapper */
    public final EmbedThumbnail getRawThumbnail() {
        return embed.h();
    }

    public final ThumbnailWrapper getThumbnail() {
        return new ThumbnailWrapper(getRawThumbnail());
    }

    /** Returns the raw (obfuscated) {@link EmbedImage} Object associated with this wrapper */
    public final EmbedImage getRawImage() {
        return embed.f();
    }

    public final ImageWrapper getImage() {
        return new ImageWrapper(getRawImage());
    }

    /** Returns the raw (obfuscated) {@link EmbedVideo} Object associated with this wrapper */
    public final EmbedVideo getRawVideo() {
        return embed.m();
    }

    public final VideoWrapper getVideo() {
        return new VideoWrapper(getRawVideo());
    }

    /** Returns the raw (obfuscated) {@link EmbedProvider} Object associated with this wrapper */
    public final EmbedProvider getRawProvider() {
        return embed.g();
    }

    public final ProviderWrapper getProvider() {
        return new ProviderWrapper(getRawProvider());
    }

    public final UtcDateTime getTimestamp() {
        return embed.i();
    }

    public final String getTitle() {
        return embed.j();
    }

    public final EmbedType getType() {
        return embed.k();
    }

    public final String getUrl() {
        return embed.l();
    }
}
