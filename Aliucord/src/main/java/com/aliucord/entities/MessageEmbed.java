package com.aliucord.entities;

import com.aliucord.Main;
import com.discord.models.domain.ModelMessageEmbed;

import java.util.List;

@SuppressWarnings("unused")
public class MessageEmbed extends ModelMessageEmbed {
    public static class Field extends ModelMessageEmbed.Field {
        private static java.lang.reflect.Field inline;
        private static java.lang.reflect.Field name;
        private static java.lang.reflect.Field value;

        static {
            try {
                Class<ModelMessageEmbed.Field> c = ModelMessageEmbed.Field.class;
                inline = c.getDeclaredField("inline");
                inline.setAccessible(true);
                name = c.getDeclaredField("name");
                name.setAccessible(true);
                value = c.getDeclaredField("value");
                value.setAccessible(true);
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
                inline.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }

        public void setName(String v) {
            try {
                name.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }

        public void setValue(String v) {
            try {
                value.set(this, v);
            } catch (IllegalAccessException e) { Main.logger.error(e); }
        }
    }

    // reflect moment
    private static java.lang.reflect.Field attachment;
    private static java.lang.reflect.Field author;
    private static java.lang.reflect.Field color;
    private static java.lang.reflect.Field description;
    private static java.lang.reflect.Field fields;
    private static java.lang.reflect.Field footer;
    private static java.lang.reflect.Field image;
    private static java.lang.reflect.Field provider;
    private static java.lang.reflect.Field referenceId;
    private static java.lang.reflect.Field thumbnail;
    private static java.lang.reflect.Field timestamp;
    private static java.lang.reflect.Field title;
    private static java.lang.reflect.Field type;
    private static java.lang.reflect.Field url;
    private static java.lang.reflect.Field video;

    static {
        try {
            Class<ModelMessageEmbed> c = ModelMessageEmbed.class;
            attachment = c.getDeclaredField("attachment");
            attachment.setAccessible(true);
            author = c.getDeclaredField("author");
            author.setAccessible(true);
            color = c.getDeclaredField("color");
            color.setAccessible(true);
            description = c.getDeclaredField("description");
            description.setAccessible(true);
            fields = c.getDeclaredField("fields");
            fields.setAccessible(true);
            footer = c.getDeclaredField("footer");
            footer.setAccessible(true);
            image = c.getDeclaredField("image");
            image.setAccessible(true);
            provider = c.getDeclaredField("provider");
            provider.setAccessible(true);
            referenceId = c.getDeclaredField("referenceId");
            referenceId.setAccessible(true);
            thumbnail = c.getDeclaredField("thumbnail");
            thumbnail.setAccessible(true);
            timestamp = c.getDeclaredField("timestamp");
            timestamp.setAccessible(true);
            title = c.getDeclaredField("title");
            title.setAccessible(true);
            type = c.getDeclaredField("type");
            type.setAccessible(true);
            url = c.getDeclaredField("url");
            url.setAccessible(true);
            video = c.getDeclaredField("video");
            video.setAccessible(true);
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
            attachment.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setAuthor(String v) {
        try {
            author.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setColor(Integer v) {
        try {
            color.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setDescription(String v) {
        try {
            description.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setFields(List<ModelMessageEmbed.Field> v) {
        try {
            fields.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setFooter(Item v) {
        try {
            footer.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setImage(Item v) {
        try {
            image.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setProvider(Item v) {
        try {
            provider.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setReferenceId(Long v) {
        try {
            referenceId.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setThumbnail(Item v) {
        try {
            thumbnail.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setTimestamp(String v) {
        try {
            timestamp.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setTitle(String v) {
        try {
            title.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setType(String v) {
        try {
            type.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setUrl(String v) {
        try {
            url.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }

    public void setVideo(String v) {
        try {
            video.set(this, v);
        } catch (IllegalAccessException e) { Main.logger.error(e); }
    }
}
