/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

import com.discord.api.message.embed.*;
import com.discord.api.utcdatetime.UtcDateTime;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Wraps the obfuscated {@link MessageEmbed} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class MessageEmbedWrapper {
    /**
     * Wraps the obfuscated {@link EmbedAuthor} class to provide nice method names and require only one central
     * update if method names change after an update
     */
    public static class EmbedAuthorWrapper {
        private final EmbedAuthor author;

        public EmbedAuthorWrapper(EmbedAuthor author) {
            this.author = author;
        }

        public final EmbedAuthor raw() {
            return author;
        }

        public final String getName() {
            return author.a();
        }

        public final String getProxyIconUrl() {
            return author.b();
        }

        public final String getUrl() {
            return author.c();
        }
    }

    /**
     * Wraps the obfuscated {@link EmbedField} class to provide nice method names and require only one central
     * update if method names change after an update
     */
    public static class EmbedFieldWrapper {
        private static Field inlineField;
        static {
            try {
                inlineField = EmbedField.class.getDeclaredField("inline");
                inlineField.setAccessible(true);
            } catch (Throwable ignored) {}
        }

        private final EmbedField field;

        public EmbedFieldWrapper(EmbedField field) {
            this.field = field;
        }

        public final EmbedField raw() {
            return field;
        }

        public final String getName() {
            return field.a();
        }

        public final String getValue() {
            return field.b();
        }

        public final boolean isInline() {
            try {
                return inlineField.get(field) == Boolean.TRUE;
            } catch (Throwable ignored) {}
            return false;
        }
    }

    /**
     * Wraps the obfuscated {@link EmbedFooter} class to provide nice method names and require only one central
     * update if method names change after an update
     */
    public static final class EmbedFooterWrapper {
        private final EmbedFooter footer;

        public EmbedFooterWrapper(EmbedFooter footer) {
            this.footer = footer;
        }

        public final EmbedFooter raw() {
            return footer;
        }

        public final String getProxyIconUrl() {
            return footer.a();
        }

        public final String getText() {
            return footer.b();
        }
    }

    /**
     * Wraps the obfuscated {@link EmbedImage} class to provide nice method names and require only one central
     * update if method names change after an update
     */
    public static class EmbedImageWrapper {
        private final EmbedImage image;

        public EmbedImageWrapper(EmbedImage image) {
            this.image = image;
        }

        public final EmbedImage raw() {
            return image;
        }

        public final Integer getHeight() {
            return image.a();
        }

        public final String getProxyUrl() {
            return image.b();
        }

        public final String getUrl() {
            return image.c();
        }

        public final Integer getWidth() {
            return image.d();
        }
    }

    private final MessageEmbed embed;

    public MessageEmbedWrapper(MessageEmbed embed) {
        this.embed = embed;
    }

    public final MessageEmbed raw() {
        return embed;
    }

    public final EmbedAuthorWrapper getAuthor() {
        return new EmbedAuthorWrapper(getRawAuthor());
    }

    public final EmbedAuthor getRawAuthor() {
        return embed.a();
    }

    public final Integer getColor() {
        return embed.b();
    }

    public final String getDescription() {
        return embed.c();
    }

    public final List<EmbedField> getFields() {
        return embed.d();
    }

    public final EmbedFooterWrapper getFooter() {
        return new EmbedFooterWrapper(getRawFooter());
    }

    public final EmbedFooter getRawFooter() {
        return embed.e();
    }

    public final EmbedImageWrapper getImage() {
        return new EmbedImageWrapper(getRawImage());
    }

    public final EmbedImage getRawImage() {
        return embed.f();
    }

    public final EmbedProvider getProvider() {
        return embed.g();
    }

    public final EmbedThumbnail getThumbnail() {
        return embed.h();
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

    public final EmbedVideo getVideo() {
        return embed.m();
    }
}
