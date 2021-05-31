/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities;

import com.aliucord.Main;
import com.aliucord.utils.ReflectUtils;
import com.discord.api.message.embed.*;
import com.discord.models.domain.ModelMessageEmbed;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class MessageEmbed extends ModelMessageEmbed {
    // reflect moment
    private static Field attachmentField;
    private static Field authorField;
    private static Field colorField;
    private static Field descriptionField;
    private static Field fieldsField;
    private static Field footerField;
    private static Field imageField;
    private static Field providerField;
    private static Field referenceIdField;
    private static Field thumbnailField;
    private static Field timestampField;
    private static Field titleField;
    private static Field typeField;
    private static Field urlField;
    private static Field videoField;

    static {
        try {
            Class<ModelMessageEmbed> c = ModelMessageEmbed.class;
            attachmentField = c.getDeclaredField("attachment");
            attachmentField.setAccessible(true);
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
            referenceIdField = c.getDeclaredField("referenceId");
            referenceIdField.setAccessible(true);
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

    public MessageEmbed() {
        this(RICH);
    }

    public MessageEmbed(String type) {
        super();
        setType(type);
    }

    public MessageEmbed setAttachment(boolean v) {
        try {
            attachmentField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setAuthor(String name, String iconUrl, String url) {
        EmbedAuthor author = new EmbedAuthor();
        Class<EmbedAuthor> c = EmbedAuthor.class;
        try {
            ReflectUtils.setField(c, author, "name", name, true);
            ReflectUtils.setField(c, author, "proxyIconUrl", iconUrl, true);
            ReflectUtils.setField(c, author, "url", url, true);
        } catch (Throwable e) { Main.logger.error(e); }
        return setAuthor(author);
    }
    public MessageEmbed setAuthor(EmbedAuthor v) {
        try {
            authorField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setColor(Integer v) {
        try {
            colorField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setDescription(String v) {
        try {
            descriptionField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed addField(String name, String value, boolean inline) {
        return addField(createField(name, value, inline));
    }
    @SuppressWarnings("unchecked")
    public MessageEmbed addField(EmbedField v) {
        try {
            List<EmbedField> o = (List<EmbedField>) fieldsField.get(this);
            if (o == null) fieldsField.set(this, Collections.singletonList(v));
            else {
                ArrayList<EmbedField> aList = (o instanceof ArrayList ? (ArrayList<EmbedField>) o : new ArrayList<>(o));
                aList.add(v);
                fieldsField.set(this, aList);
            }
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setFields(List<EmbedField> v) {
        try {
            fieldsField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setFooter(String text, String iconUrl) {
        EmbedFooter footer = new EmbedFooter();
        Class<EmbedFooter> c = EmbedFooter.class;
        try {
            ReflectUtils.setField(c, footer, "text", text, true);
            ReflectUtils.setField(c, footer, "proxyIconUrl", iconUrl, true);
        } catch (Throwable e) { Main.logger.error(e); }
        return setFooter(footer);
    }
    public MessageEmbed setFooter(EmbedFooter v) {
        try {
            footerField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setImage(EmbedImage v) {
        try {
            imageField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setProvider(EmbedProvider v) {
        try {
            providerField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setReferenceId(Long v) {
        try {
            referenceIdField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setThumbnail(EmbedThumbnail v) {
        try {
            thumbnailField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setTimestamp(String v) {
        try {
            timestampField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setTitle(String v) {
        try {
            titleField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setType(String v) {
        try {
            typeField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setUrl(String v) {
        try {
            urlField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setVideo(String v) {
        try {
            videoField.set(this, v);
        } catch (Throwable e) { Main.logger.error(e); }
        return this;
    }

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
