/*
 * Copyright (c) 2021 Juby210 & Vendicated
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
        return getRawAuthor(embed);
    }

    @Nullable
    public final AuthorWrapper getAuthor() {
        return getAuthor(embed);
    }

    @Nullable
    public final Integer getColor() {
        return getColor(embed);
    }

    @Nullable
    public final String getDescription() {
        return getDescription(embed);
    }

    /** Returns the raw (obfuscated) {@link EmbedField}s associated with this wrapper */
    public final List<EmbedField> getRawFields() {
        return getRawFields(embed);
    }

    public final List<FieldWrapper> getFields() {
        return getFields(embed);
    }

    /** Returns the raw (obfuscated) {@link EmbedFooter} Object associated with this wrapper */
    @Nullable
    public final EmbedFooter getRawFooter() {
        return getRawFooter(embed);
    }

    @Nullable
    public final FooterWrapper getFooter() {
        return getFooter(embed);
    }

    /** Returns the raw (obfuscated) {@link EmbedThumbnail} Object associated with this wrapper */
    @Nullable
    public final EmbedThumbnail getRawThumbnail() {
        return getRawThumbnail(embed);
    }

    @Nullable
    public final ThumbnailWrapper getThumbnail() {
        return getThumbnail(embed);
    }

    /** Returns the raw (obfuscated) {@link EmbedImage} Object associated with this wrapper */
    @Nullable
    public final EmbedImage getRawImage() {
        return getRawImage(embed);
    }

    @Nullable
    public final ImageWrapper getImage() {
        return getImage(embed);
    }

    /** Returns the raw (obfuscated) {@link EmbedVideo} Object associated with this wrapper */
    @Nullable
    public final EmbedVideo getRawVideo() {
        return getRawVideo(embed);
    }

    @Nullable
    public final VideoWrapper getVideo() {
        return getVideo(embed);
    }

    /** Returns the raw (obfuscated) {@link EmbedProvider} Object associated with this wrapper */
    @Nullable
    public final EmbedProvider getRawProvider() {
        return getRawProvider(embed);
    }

    @Nullable
    public final ProviderWrapper getProvider() {
        return getProvider(embed);
    }

    @Nullable
    public final UtcDateTime getTimestamp() {
        return getTimestamp(embed);
    }

    @Nullable
    public final String getTitle() {
        return getTitle(embed);
    }

    public final EmbedType getType() {
        return getType(embed);
    }

    @Nullable
    public final String getUrl() {
        return getUrl(embed);
    }



    /** Returns the raw (obfuscated) {@link EmbedAuthor} Object associated with this wrapper */
    @Nullable
    public static EmbedAuthor getRawAuthor(MessageEmbed embed) {
        return embed.a();
    }

    @Nullable
    public static AuthorWrapper getAuthor(MessageEmbed embed) {
        EmbedAuthor author = getRawAuthor(embed);
        if (author == null) return null;
        return new AuthorWrapper(author);
    }

    @Nullable
    public static Integer getColor(MessageEmbed embed) {
        return embed.b();
    }

    @Nullable
    public static String getDescription(MessageEmbed embed) {
        return embed.c();
    }

    /** Returns the raw (obfuscated) {@link EmbedField}s associated with this wrapper */
    public static List<EmbedField> getRawFields(MessageEmbed embed) {
        return embed.d();
    }

    public static List<FieldWrapper> getFields(MessageEmbed embed) {
        return CollectionUtils.map(getRawFields(embed), FieldWrapper::new);
    }

    /** Returns the raw (obfuscated) {@link EmbedFooter} Object associated with this wrapper */
    @Nullable
    public static EmbedFooter getRawFooter(MessageEmbed embed) {
        return embed.e();
    }

    @Nullable
    public static FooterWrapper getFooter(MessageEmbed embed) {
        EmbedFooter footer = getRawFooter(embed);
        if (footer == null) return null;
        return new FooterWrapper(footer);
    }

    /** Returns the raw (obfuscated) {@link EmbedThumbnail} Object associated with this wrapper */
    @Nullable
    public static EmbedThumbnail getRawThumbnail(MessageEmbed embed) {
        return embed.h();
    }

    @Nullable
    public static ThumbnailWrapper getThumbnail(MessageEmbed embed) {
        EmbedThumbnail thumb = getRawThumbnail(embed);
        if (thumb == null) return null;
        return new ThumbnailWrapper(thumb);
    }

    /** Returns the raw (obfuscated) {@link EmbedImage} Object associated with this wrapper */
    @Nullable
    public static EmbedImage getRawImage(MessageEmbed embed) {
        return embed.f();
    }

    @Nullable
    public static ImageWrapper getImage(MessageEmbed embed) {
        EmbedImage img = getRawImage(embed);
        if (img == null) return null;
        return new ImageWrapper(img);
    }

    /** Returns the raw (obfuscated) {@link EmbedVideo} Object associated with this wrapper */
    @Nullable
    public static EmbedVideo getRawVideo(MessageEmbed embed) {
        return embed.m();
    }

    @Nullable
    public static VideoWrapper getVideo(MessageEmbed embed) {
        EmbedVideo video = getRawVideo(embed);
        if (video == null) return null;
        return new VideoWrapper(video);
    }

    /** Returns the raw (obfuscated) {@link EmbedProvider} Object associated with this wrapper */
    @Nullable
    public static EmbedProvider getRawProvider(MessageEmbed embed) {
        return embed.g();
    }

    @Nullable
    public static ProviderWrapper getProvider(MessageEmbed embed) {
        EmbedProvider provider = getRawProvider(embed);
        if (provider == null) return null;
        return new ProviderWrapper(provider);
    }

    @Nullable
    public static UtcDateTime getTimestamp(MessageEmbed embed) {
        return embed.i();
    }

    @Nullable
    public static String getTitle(MessageEmbed embed) {
        return embed.j();
    }

    public static EmbedType getType(MessageEmbed embed) {
        return embed.k();
    }

    @Nullable
    public static String getUrl(MessageEmbed embed) {
        return embed.l();
    }
}
