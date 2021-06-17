/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

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
        return getName(field);
    }

    public final String getValue() {
        return getValue(field);
    }

    public final boolean isInline() {
        return isInline(field);
    }


    public static String getName(EmbedField field) {
        return field.a();
    }

    public static String getValue(EmbedField field) {
        return field.b();
    }

    public static boolean isInline(EmbedField field) {
        try {
            return inlineField.get(field) == Boolean.TRUE;
        } catch (Throwable ignored) { return false; }
    }
}
