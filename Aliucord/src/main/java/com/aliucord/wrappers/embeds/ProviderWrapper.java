package com.aliucord.wrappers.embeds;

import com.discord.api.message.embed.EmbedProvider;

/**
 * Wraps the obfuscated {@link EmbedProvider} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class ProviderWrapper {
    public final EmbedProvider provider;

    public ProviderWrapper(EmbedProvider provider) {
        this.provider = provider;
    }

    /** Returns the raw (obfuscated) {@link EmbedProvider} Object associated with this wrapper */
    public final EmbedProvider raw() {
        return provider;
    }

    public final String getName() {
        return provider.a();
    }

    public final String getUrl() {
        return provider.b();
    }
}
