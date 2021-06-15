package com.aliucord.wrappers.embeds;

import com.discord.api.message.embed.EmbedField;

import java.lang.reflect.Field;

/**
 * Wraps the obfuscated {@link EmbedField} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class FieldWrapper {
    private static Field inlineField;
    private final EmbedField field;

    static {
        try {
            inlineField = EmbedField.class.getDeclaredField("inline");
            inlineField.setAccessible(true);
        } catch (Throwable ignored) {}
    }


    public FieldWrapper(EmbedField field) {
        this.field = field;
    }

    /** Returns the raw (obfuscated) {@link EmbedField} Object associated with this wrapper */
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
        } catch (Throwable ignored) { return false; }
    }
}