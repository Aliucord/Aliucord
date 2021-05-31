package com.discord.api.message.embed;

@SuppressWarnings("unused")
public final class EmbedProvider {
    private final String name;
    private final String url;

    public EmbedProvider(String name, String url) {
        this.name = name;
        this.url = url;
    }

    // getName
    public final String a() { return name; }
    // getUrl
    public final String b() { return url; }
}
