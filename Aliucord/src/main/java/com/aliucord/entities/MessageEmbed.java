/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities;

import com.aliucord.Main;
import com.discord.models.domain.ModelMessageEmbed;

import java.util.List;

@SuppressWarnings("unused")
public class MessageEmbed extends ModelMessageEmbed {
    public static class Field extends ModelMessageEmbed.Field {
        private static java.lang.reflect.Field inlineField;
        private static java.lang.reflect.Field nameField;
        private static java.lang.reflect.Field valueField;

        static {
            try {
                Class<ModelMessageEmbed.Field> c = ModelMessageEmbed.Field.class;
                inlineField = c.getDeclaredField("inline");
                inlineField.setAccessible(true);
                nameField = c.getDeclaredField("name");
                nameField.setAccessible(true);
                valueField = c.getDeclaredField("value");
                valueField.setAccessible(true);
            } catch (Exception e) { Main.logger.error(e); }
        }

        public Field() {
            super();
        }
        public Field(String name, String value, boolean inline) {
            super();
            setName(name);
            setValue(value);
            setInline(inline);
        }

        public void setInline(boolean v) {
            try {
                inlineField.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }

        public void setName(String v) {
            try {
                nameField.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }

        public void setValue(String v) {
            try {
                valueField.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }
    }

    // reflect moment
    private static java.lang.reflect.Field attachmentField;
    private static java.lang.reflect.Field authorField;
    private static java.lang.reflect.Field colorField;
    private static java.lang.reflect.Field descriptionField;
    private static java.lang.reflect.Field fieldsField;
    private static java.lang.reflect.Field footerField;
    private static java.lang.reflect.Field imageField;
    private static java.lang.reflect.Field providerField;
    private static java.lang.reflect.Field referenceIdField;
    private static java.lang.reflect.Field thumbnailField;
    private static java.lang.reflect.Field timestampField;
    private static java.lang.reflect.Field titleField;
    private static java.lang.reflect.Field typeField;
    private static java.lang.reflect.Field urlField;
    private static java.lang.reflect.Field videoField;

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

    public void setAttachment(boolean v) {
        try {
            attachmentField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setAuthor(Item v) {
        try {
            authorField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setColor(Integer v) {
        try {
            colorField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setDescription(String v) {
        try {
            descriptionField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setFields(List<ModelMessageEmbed.Field> v) {
        try {
            fieldsField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setFooter(Item v) {
        try {
            footerField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setImage(Item v) {
        try {
            imageField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setProvider(Item v) {
        try {
            providerField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setReferenceId(Long v) {
        try {
            referenceIdField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setThumbnail(Item v) {
        try {
            thumbnailField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setTimestamp(String v) {
        try {
            timestampField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setTitle(String v) {
        try {
            titleField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setType(String v) {
        try {
            typeField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setUrl(String v) {
        try {
            urlField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setVideo(String v) {
        try {
            videoField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }
}
