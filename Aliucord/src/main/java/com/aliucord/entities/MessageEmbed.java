/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities;

import com.aliucord.Main;
import com.discord.models.domain.ModelMessageEmbed;

import java.util.ArrayList;
import java.util.Collections;
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

    public static class Author extends ModelMessageEmbed.Item {
        private static java.lang.reflect.Field nameField;
        private static java.lang.reflect.Field iconUrlField;
        private static java.lang.reflect.Field urlField;

        static {
            try {
                Class<ModelMessageEmbed.Item> c = ModelMessageEmbed.Item.class;
                nameField = c.getDeclaredField("name");
                nameField.setAccessible(true);
                iconUrlField = c.getDeclaredField("iconUrl");
                iconUrlField.setAccessible(true);
                urlField = c.getDeclaredField("url");
                urlField.setAccessible(true);
            } catch (Exception e) { Main.logger.error(e); }
        }

        public Author() {
            super();
        }
        public Author(String name) {
            this(name, null, null);
        }
        public Author(String name, String iconUrl) {
            this(name, iconUrl, null);
        }
        public Author(String name, String iconUrl, String url) {
            super();
            setName(name);
            setIconUrl(iconUrl);
            setUrl(url);
        }

        public void setIconUrl(String v) {
            try {
                iconUrlField.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }

        public void setName(String v) {
            try {
                nameField.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }

        public void setUrl(String v) {
            try {
                urlField.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }
    }

    public static class Footer extends ModelMessageEmbed.Item {
        private static java.lang.reflect.Field textField;
        private static java.lang.reflect.Field iconUrlField;

        static {
            try {
                Class<ModelMessageEmbed.Item> c = ModelMessageEmbed.Item.class;
                textField = c.getDeclaredField("text");
                textField.setAccessible(true);
                iconUrlField = c.getDeclaredField("iconUrl");
                iconUrlField.setAccessible(true);
            } catch (Exception e) { Main.logger.error(e); }
        }

        public Footer() {
            super();
        }
        public Footer(String text) {
            this(text, null);
        }
        public Footer(String name, String iconUrl) {
            super();
            setText(name);
            setIconUrl(iconUrl);
        }

        public void setIconUrl(String v) {
            try {
                iconUrlField.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }

        public void setText(String v) {
            try {
                textField.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }
    }

    public static class Image extends ModelMessageEmbed.Item {
        private static java.lang.reflect.Field urlField;

        static {
            try {
                Class<ModelMessageEmbed.Item> c = ModelMessageEmbed.Item.class;
                urlField = c.getDeclaredField("url");
                urlField.setAccessible(true);
            } catch (Exception e) { Main.logger.error(e); }
        }

        public Image() {
            super();
        }
        public Image(String url) {
            super();
            setUrl(url);
        }

        public void setUrl(String v) {
            try {
                urlField.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }
    }

    public static class Provider extends ModelMessageEmbed.Item {
        private static java.lang.reflect.Field nameField;
        private static java.lang.reflect.Field urlField;

        static {
            try {
                Class<ModelMessageEmbed.Item> c = ModelMessageEmbed.Item.class;
                nameField = c.getDeclaredField("name");
                nameField.setAccessible(true);
                urlField = c.getDeclaredField("url");
                urlField.setAccessible(true);
            } catch (Exception e) { Main.logger.error(e); }
        }

        public Provider() {
            super();
        }
        public Provider(String name) {
            this(name, null);
        }
        public Provider(String name, String url) {
            super();
            setName(name);
            setUrl(url);
        }
        
        public void setName(String v) {
            try {
                nameField.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }

        public void setUrl(String v) {
            try {
                urlField.set(this, v);
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

    public MessageEmbed setAttachment(boolean v) {
        try {
            attachmentField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setAuthor(String name, String iconUrl, String url) {
        return setAuthor(new Author(name, iconUrl, url));
    }
    public MessageEmbed setAuthor(Author v) {
        try {
            authorField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setColor(Integer v) {
        try {
            colorField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setDescription(String v) {
        try {
            descriptionField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed addField(String name, String value, boolean inline) {
        return addField(new Field(name, value, inline));
    }
    @SuppressWarnings("unchecked")
    public MessageEmbed addField(ModelMessageEmbed.Field v) {
        try {
            Object o = fieldsField.get(this);
            if (o instanceof List) {
                ArrayList<ModelMessageEmbed.Field> list = (ArrayList<ModelMessageEmbed.Field>) (o instanceof ArrayList ? o : new ArrayList<>((List<ModelMessageEmbed.Field>)o));
                list.add(v);
                fieldsField.set(this, list);
            } else {
                fieldsField.set(this, Collections.singletonList(v));
            }
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setFields(List<ModelMessageEmbed.Field> v) {
        try {
            fieldsField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setFooter(String text, String iconUrl) {
        return setFooter(new Footer(text, iconUrl));
    }
    public MessageEmbed setFooter(Footer v) {
        try {
            footerField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setImage(String v) {
        return setImage(new Image(v));
    }
    public MessageEmbed setImage(Image v) {
        try {
            imageField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setProvider(Provider v) {
        try {
            providerField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setReferenceId(Long v) {
        try {
            referenceIdField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setThumbnail(String v) {
        return setThumbnail(new Image(v));
    }
    public MessageEmbed setThumbnail(Image v) {
        try {
            thumbnailField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setTimestamp(String v) {
        try {
            timestampField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setTitle(String v) {
        try {
            titleField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setType(String v) {
        try {
            typeField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setUrl(String v) {
        try {
            urlField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }

    public MessageEmbed setVideo(String v) {
        try {
            videoField.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
        return this;
    }
}
