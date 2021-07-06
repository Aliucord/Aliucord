/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities;

import com.aliucord.Main;
import com.aliucord.utils.ReflectUtils;
import com.discord.api.message.embed.*;
import com.discord.api.utcdatetime.UtcDateTime;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused"})
public class MessageEmbedBuilder {
    // reflect moment
    private static Field authorField;
    private static Field colorField;
    private static Field descriptionField;
    private static Field fieldsField;
    private static Field footerField;
    private static Field imageField;
    private static Field providerField;
    private static Field thumbnailField;
    private static Field timestampField;
    private static Field titleField;
    private static Field typeField;
    private static Field urlField;
    private static Field videoField;

    static {
        try {
            Class<com.discord.api.message.embed.MessageEmbed> c = com.discord.api.message.embed.MessageEmbed.class;
            authorField = c.getDeclaredField("author");
            authorField.setAccessible(true);
            colorField = c.getDeclaredField("color");
            colorField.setAccessible(true);
            descriptionField = c.getDeclaredField("description");
            descriptionField.setAccessible(true);
            fieldsField = c.getDeclaredField("fields");
            fieldsField.setAccessible(true);
            footerField = c.getDeclaredField("footer");
            footerField.setAccessible(true);
            imageField = c.getDeclaredField("image");
            imageField.setAccessible(true);
            providerField = c.getDeclaredField("provider");
            providerField.setAccessible(true);
            thumbnailField = c.getDeclaredField("thumbnail");
            thumbnailField.setAccessible(true);
            timestampField = c.getDeclaredField("timestamp");
            timestampField.setAccessible(true);
            titleField = c.getDeclaredField("title");
            titleField.setAccessible(true);
            typeField = c.getDeclaredField("type");
            typeField.setAccessible(true);
            urlField = c.getDeclaredField("url");
            urlField.setAccessible(true);
            videoField = c.getDeclaredField("video");
            videoField.setAccessible(true);
        } catch (Exception e) { Main.logger.error(e); }
    }

    private final com.discord.api.message.embed.MessageEmbed embed;

    /**
     * Creates a rich embed
     */
    public MessageEmbedBuilder() {
        this(EmbedType.RICH);
    }

    /**
     * Creates an embed with a specific type.
     * @param type {@link EmbedType}
     */
    public MessageEmbedBuilder(EmbedType type) {
        embed = new com.discord.api.message.embed.MessageEmbed();
        setType(type);
    }

    /** Builds the MessageEmbed */
    public com.discord.api.message.embed.MessageEmbed build() {
        return embed;
    }

    /**
     * @param name Name of the author.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setAuthor(String name) {
        return setAuthor(name, null, null);
    }

    /**
     * @param name Name of the author.
     * @param iconUrl Icon URL of the author.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setAuthor(String name, String iconUrl) {
        return setAuthor(name, iconUrl, iconUrl);
    }

    /**
     * @param name Name of the author.
     * @param proxyIconUrl Proxy icon URL of the author.
     * @param iconUrl Icon URL of the author.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setAuthor(String name, String iconUrl, String proxyIconUrl) {
        EmbedAuthor author = new EmbedAuthor();
        Class<EmbedAuthor> c = EmbedAuthor.class;
        try {
            ReflectUtils.setField(c, author, "name", name, true);
            ReflectUtils.setField(c, author, "iconUrl", iconUrl, true);
            ReflectUtils.setField(c, author, "proxyIconUrl", proxyIconUrl, true);
        } catch (Throwable e) { Main.logger.error(e); }
        return setAuthor(author);
    }

    /**
     * Sets the embed author.
     * @param author {@link EmbedAuthor}
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setAuthor(EmbedAuthor author) {
        try {
            authorField.set(embed, author);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets a random embed color.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setRandomColor() {
        return setColor(new java.util.Random().nextInt(0xffffff + 1));
    }

    /**
     * Sets the embed color.
     * @param color Embed color.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setColor(Integer color) {
        try {
            colorField.set(embed, color);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets the embed description.
     * @param description Embed description.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setDescription(String description) {
        try {
            descriptionField.set(embed, description);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Adds a field to the embed.
     * @param name Name of the field.
     * @param value Content of the field.
     * @param inline Whether to inline the field or not.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder addField(String name, String value, boolean inline) {
        return addField(createField(name, value, inline));
    }

    /**
     * Adds a field to the embed.
     * @param field {@link EmbedField}
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#addField(String, String, boolean)
     * @see MessageEmbedBuilder#createField(String, String, Boolean)
     */
    public MessageEmbedBuilder addField(EmbedField field) {
        try {
            List<EmbedField> o = (List<EmbedField>) fieldsField.get(embed);
            if (o == null) fieldsField.set(embed, Collections.singletonList(field));
            else {
                ArrayList<EmbedField> aList = (o instanceof ArrayList ? (ArrayList<EmbedField>) o : new ArrayList<>(o));
                aList.add(field);
                fieldsField.set(embed, aList);
            }
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets embed fields.
     * @param fields {@link List} of {@link EmbedField}
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#createField(String, String, Boolean)
     */
    public MessageEmbedBuilder setFields(List<EmbedField> fields) {
        try {
            fieldsField.set(embed, fields);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets the embed footer.
     * @param text Footer text.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setFooter(String text) {
        return setFooter(text, null, null);
    }

    /**
     * Sets the embed footer.
     * @param text Footer text.
     * @param iconUrl Footer icon URL.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setFooter(String text, String iconUrl) {
        return setFooter(text, iconUrl, iconUrl);
    }

    /**
     * Sets the embed footer.
     * @param text Footer text.
     * @param iconUrl Footer icon URL.
     * @param proxyIconUrl Footer Proxy icon URL.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setFooter(String text, String iconUrl, String proxyIconUrl) {
        EmbedFooter footer = new EmbedFooter();
        Class<EmbedFooter> c = EmbedFooter.class;
        try {
            ReflectUtils.setField(c, footer, "text", text, true);
            ReflectUtils.setField(c, footer, "iconUrl", iconUrl, true);
            ReflectUtils.setField(c, footer, "proxyIconUrl", proxyIconUrl, true);
        } catch (Throwable e) { Main.logger.error(e); }
        return setFooter(footer);
    }

    /**
     * Sets the embed footer.
     * @param footer {@link EmbedFooter}
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#setFooter(String, String, String)
     */
    public MessageEmbedBuilder setFooter(EmbedFooter footer) {
        try {
            footerField.set(embed, footer);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets the embed image.
     * @param imageUrl Image URL.
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#setImage(String, String, Integer, Integer)
     */
    public MessageEmbedBuilder setImage(String imageUrl) {
        return setImage(imageUrl, imageUrl, 512, 512);
    }

    /**
     * Sets the embed image.
     * @param imageUrl Image URL.
     * @param proxyImageUrl Proxy image URL.
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#setImage(String, String, Integer, Integer)
     */
    public MessageEmbedBuilder setImage(String imageUrl, String proxyImageUrl) {
        return setImage(imageUrl, proxyImageUrl, 512, 512);
    }

    /**
     * Sets the embed image.
     * @param imageUrl Image URL.
     * @param proxyImageUrl Proxy image URL.
     * @param imageHeight Image height.
     * @param imageWidth Image width.
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#setImage(String, String)
     */
    public MessageEmbedBuilder setImage(String imageUrl, String proxyImageUrl, Integer imageHeight, Integer imageWidth) {
        EmbedImage image = new EmbedImage();
        Class<EmbedImage> c = EmbedImage.class;
        try {
            ReflectUtils.setField(c, image, "url", imageUrl, true);
            ReflectUtils.setField(c, image, "proxyUrl", proxyImageUrl, true);
            ReflectUtils.setField(c, image, "height", imageHeight, true);
            ReflectUtils.setField(c, image, "width", imageWidth, true);
        } catch (Throwable e) { Main.logger.error(e); }
        return setImage(image);
    }

    /**
     * Sets the embed image.
     * @param image {@link EmbedImage}
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#setImage(String, String)
     */
    public MessageEmbedBuilder setImage(EmbedImage image) {
        try {
            imageField.set(embed, image);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets the embed provider.
     * @param provider {@link EmbedProvider}.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setProvider(EmbedProvider provider) {
        try {
            providerField.set(embed, provider);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets the embed thumbnail.
     * @param imageUrl Image URL.
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#setThumbnail(String, String, Integer, Integer)
     */
    public MessageEmbedBuilder setThumbnail(String imageUrl) {
        return setThumbnail(imageUrl, imageUrl, 512, 512);
    }

    /**
     * Sets the embed thumbnail.
     * @param imageUrl Image URL.
     * @param proxyImageUrl Proxy image URL.
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#setThumbnail(String, String, Integer, Integer)
     */
    public MessageEmbedBuilder setThumbnail(String imageUrl, String proxyImageUrl) {
        return setThumbnail(imageUrl, proxyImageUrl, 512, 512);
    }

    /**
     * Sets the embed thumbnail.
     * @param imageUrl Image URL.
     * @param proxyImageUrl Proxy image URL.
     * @param imageHeight Image height.
     * @param imageWidth Image width.
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#setThumbnail(String, String)
     */
    public MessageEmbedBuilder setThumbnail(String imageUrl, String proxyImageUrl, Integer imageHeight, Integer imageWidth) {
        EmbedThumbnail image = new EmbedThumbnail();
        Class<EmbedThumbnail> c = EmbedThumbnail.class;
        try {
            ReflectUtils.setField(c, image, "url", imageUrl, true);
            ReflectUtils.setField(c, image, "proxyUrl", proxyImageUrl, true);
            ReflectUtils.setField(c, image, "height", imageHeight, true);
            ReflectUtils.setField(c, image, "width", imageWidth, true);
        } catch (Throwable e) { Main.logger.error(e); }
        return setThumbnail(image);
    }

    /**
     * Sets the embed thumbnail.
     * @param image {@link EmbedThumbnail}
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#setThumbnail(String, String)
     */
    public MessageEmbedBuilder setThumbnail(EmbedThumbnail image) {
        try {
            thumbnailField.set(embed, image);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets the embed timestamp.
     * @param timestamp {@link UtcDateTime} timestamp.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setTimestamp(UtcDateTime timestamp) {
        try {
            timestampField.set(embed, timestamp);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets the embed title.
     * @param title Embed title.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setTitle(String title) {
        try {
            titleField.set(embed, title);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets the embed type.
     * @param type {@link EmbedType}.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setType(EmbedType type) {
        try {
            typeField.set(embed, type);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets the embed URL.
     * @param url Embed URL.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setUrl(String url) {
        try {
            urlField.set(embed, url);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * Sets the embed video.
     * @param videoUrl Video URL.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setVideo(String videoUrl, String proxyVideoUrl, Integer height, Integer width) {
        EmbedVideo video = new EmbedVideo();
        Class<EmbedVideo> c = EmbedVideo.class;
        try {
            ReflectUtils.setField(c, video, "url", videoUrl, true);
            ReflectUtils.setField(c, video, "proxyUrl", proxyVideoUrl, true);
            ReflectUtils.setField(c, video, "height", height, true);
            ReflectUtils.setField(c, video, "width", width, true);
        } catch (Throwable e) { Main.logger.error(e); }
        return setVideo(video);
    }

    /**
     * Sets the embed video.
     * @param videoUrl Video URL.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setVideo(String videoUrl) {
        return setVideo(videoUrl, videoUrl, 512, 512);
    }

    /**
     * Sets the embed video.
     * @param videoUrl Video URL.
     * @return {@link MessageEmbedBuilder} for chaining.
     */
    public MessageEmbedBuilder setVideo(String videoUrl, String proxyVideoUrl) {
        return setVideo(videoUrl, proxyVideoUrl, 512, 512);
    }

    /**
     * Sets the embed video.
     * @param video {@link EmbedVideo}.
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#setVideo(String, String)
     */
    public MessageEmbedBuilder setVideo(EmbedVideo video) {
        try {
            videoField.set(embed, video);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    /**
     * @param name Field name.
     * @param value Field content.
     * @param inline Whether to inline the field or not.
     * @return {@link MessageEmbedBuilder} for chaining.
     * @see MessageEmbedBuilder#addField(EmbedField)
     */
    public static EmbedField createField(String name, String value, Boolean inline) {
        EmbedField field = new EmbedField();
        Class<EmbedField> c = EmbedField.class;
        try {
            ReflectUtils.setField(c, field, "name", name, true);
            ReflectUtils.setField(c, field, "value", value, true);
            ReflectUtils.setField(c, field, "inline", inline, true);
        } catch (Throwable e) { Main.logger.error(e); }
        return field;
    }
}
