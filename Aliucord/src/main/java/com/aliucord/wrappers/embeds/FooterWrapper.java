/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.embeds;

import androidx.annotation.Nullable;

import com.discord.api.message.embed.EmbedFooter;

import java.lang.reflect.Field;

/**
 * Wraps the obfuscated {@link EmbedFooter} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class FooterWrapper {
    private static Field iconUrlField;
    static {
        try {
            // why is there no getter for this lol
            // FIXME: Do this without reflection once Discord adds getter
            iconUrlField = EmbedFooter.class.getDeclaredField("iconUrl");
            iconUrlField.setAccessible(true);
        } catch (Throwable ignored) {}
    }

    private final EmbedFooter footer;

    public FooterWrapper(EmbedFooter footer) {
        this.footer = footer;
    }

    /** Returns the raw (obfuscated) {@link EmbedFooter} Object associated with this wrapper */
    public final EmbedFooter raw() {
        return footer;
    }

    public final String getText() {
        return getText(footer);
    }

    @Nullable
    public final String getIconUrl() {
        return  getIconUrl(footer);
    }

    @Nullable
    public final String getProxyIconUrl() {
        return getProxyIconUrl(footer);
    }


    public static String getText(EmbedFooter footer) {
        return footer.b();
    }

    @Nullable
    public static String getIconUrl(EmbedFooter footer) {
        try {
            return (String) iconUrlField.get(footer);
        } catch (Throwable ignored) { return null; }
    }

    @Nullable
    public static String getProxyIconUrl(EmbedFooter footer) {
        return footer.a();
    }
}
