/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import java.util.Collection;
import java.util.List;

/**
 * Wraps the obfuscated {@link MessageEmbed} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class MessageEmbedWrapper {
    private final MessageEmbed embed;

    /** Wraps a list of {@link MessageEmbed}s */
    public static List<MessageEmbedWrapper> wrapList(Collection<MessageEmbed> embeds) {
        return CollectionUtils.map(embeds, MessageEmbedWrapper::new);
    }

    public MessageEmbedWrapper(MessageEmbed embed) {
        this.embed = embed;
    }

    /** Returns the raw (obfuscated) {@link MessageEmbed} Object associated with this wrapper */
    @NonNull
    public final MessageEmbed raw() {
        return embed;
    }

    /** Returns the raw (obfuscated) {@link EmbedAuthor} Object associated with this wrapper */
    @Nullable
    public final EmbedAuthor getRawAuthor() {
        return embed.a();
    }

    @Nullable
    public final AuthorWrapper getAuthor() {
        EmbedAuthor author = getRawAuthor();
        if (author == null) return null;
        return new AuthorWrapper(author);
    }

    @Nullable
    public final Integer getColor() {
        return embed.b();
    }

    @NonNull
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
    @Nullable
    public final EmbedFooter getRawFooter() {
        return embed.e();
    }

    @Nullable
    public final FooterWrapper getFooter() {
        EmbedFooter footer = getRawFooter();
        if (footer == null) return null;
        return new FooterWrapper(footer);
    }

    /** Returns the raw (obfuscated) {@link EmbedThumbnail} Object associated with this wrapper */
    @Nullable
    public final EmbedThumbnail getRawThumbnail() {
        return embed.h();
    }

    @Nullable
    public final ThumbnailWrapper getThumbnail() {
        EmbedThumbnail thumb = getRawThumbnail();
        if (thumb == null) return null;
        return new ThumbnailWrapper(thumb);
    }

    /** Returns the raw (obfuscated) {@link EmbedImage} Object associated with this wrapper */
    @Nullable
    public final EmbedImage getRawImage() {
        return embed.f();
    }

    @Nullable
    public final ImageWrapper getImage() {
        EmbedImage img = getRawImage();
        if (img == null) return null;
        return new ImageWrapper(img);
    }

    /** Returns the raw (obfuscated) {@link EmbedVideo} Object associated with this wrapper */
    @Nullable
    public final EmbedVideo getRawVideo() {
        return embed.m();
    }

    @Nullable
    public final VideoWrapper getVideo() {
        EmbedVideo video = getRawVideo();
        if (video == null) return null;
        return new VideoWrapper(getRawVideo());
    }

    /** Returns the raw (obfuscated) {@link EmbedProvider} Object associated with this wrapper */
    @Nullable
    public final EmbedProvider getRawProvider() {
        return embed.g();
    }

    @Nullable
    public final ProviderWrapper getProvider() {
        EmbedProvider provider = getRawProvider();
        if (provider == null) return null;
        return new ProviderWrapper(provider);
    }

    @Nullable
    public final UtcDateTime getTimestamp() {
        return embed.i();
    }

    @Nullable
    public final String getTitle() {
        return embed.j();
    }

    public final EmbedType getType() {
        return embed.k();
    }

    @Nullable
    public final String getUrl() {
        return embed.l();
    }
}
