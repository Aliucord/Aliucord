package com.aliucord.wrappers.embeds;

import com.discord.api.message.embed.EmbedFooter;

/**
 * Wraps the obfuscated {@link EmbedFooter} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class FooterWrapper {
    private final EmbedFooter footer;

    public FooterWrapper(EmbedFooter footer) {
        this.footer = footer;
    }

    /** Returns the raw (obfuscated) {@link EmbedFooter} Object associated with this wrapper */
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
