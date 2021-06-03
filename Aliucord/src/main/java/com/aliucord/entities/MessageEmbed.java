/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities;

import com.discord.api.message.embed.*;
import com.discord.api.utcdatetime.UtcDateTime;

import java.util.List;

/** @deprecated Use MessageEmbedBuilder instead */
@Deprecated
@SuppressWarnings("unused")
public class MessageEmbed extends MessageEmbedBuilder {
    public MessageEmbed() { super(); }

    public MessageEmbed(EmbedType type) { super(type); }

    @Override
    public MessageEmbed setAuthor(String name, String iconUrl, String url) {
        return (MessageEmbed) super.setAuthor(name, iconUrl, url);
    }

    @Override
    public MessageEmbed setAuthor(EmbedAuthor v) {
        return (MessageEmbed) super.setAuthor(v);
    }

    @Override
    public MessageEmbed setColor(Integer v) {
        return (MessageEmbed) super.setColor(v);
    }

    @Override
    public MessageEmbed setDescription(String v) {
        return (MessageEmbed) super.setDescription(v);
    }

    @Override
    public MessageEmbed addField(String name, String value, boolean inline) {
        return (MessageEmbed) super.addField(name, value, inline);
    }

    @Override
    public MessageEmbed addField(EmbedField v) {
        return (MessageEmbed) super.addField(v);
    }

    @Override
    public MessageEmbed setFields(List<EmbedField> v) {
        return (MessageEmbed) super.setFields(v);
    }

    @Override
    public MessageEmbed setFooter(String text, String iconUrl) {
        return (MessageEmbed) super.setFooter(text, iconUrl);
    }

    @Override
    public MessageEmbed setFooter(EmbedFooter v) {
        return (MessageEmbed) super.setFooter(v);
    }

    @Override
    public MessageEmbed setImage(EmbedImage v) {
        return (MessageEmbed) super.setImage(v);
    }

    @Override
    public MessageEmbed setProvider(EmbedProvider v) {
        return (MessageEmbed) super.setProvider(v);
    }

    @Override
    public MessageEmbed setThumbnail(EmbedThumbnail v) {
        return (MessageEmbed) super.setThumbnail(v);
    }

    @Override
    public MessageEmbed setTimestamp(UtcDateTime v) {
        return (MessageEmbed) super.setTimestamp(v);
    }

    @Override
    public MessageEmbed setTitle(String v) {
        return (MessageEmbed) super.setTitle(v);
    }

    @Override
    public MessageEmbed setType(EmbedType v) {
        return (MessageEmbed) super.setType(v);
    }

    @Override
    public MessageEmbed setUrl(String v) {
        return (MessageEmbed) super.setUrl(v);
    }

    @Override
    public MessageEmbed setVideo(EmbedVideo v) {
        return (MessageEmbed) super.setVideo(v);
    }
}
