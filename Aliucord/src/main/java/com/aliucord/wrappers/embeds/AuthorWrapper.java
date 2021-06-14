package com.aliucord.wrappers.embeds;

import com.discord.api.message.embed.EmbedAuthor;

/**
 * Wraps the obfuscated {@link EmbedAuthor} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class AuthorWrapper {
    private final EmbedAuthor author;

    public AuthorWrapper(EmbedAuthor author) {
        this.author = author;
    }

    /** Returns the raw (obfuscated) {@link EmbedAuthor} Object associated with this wrapper */
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