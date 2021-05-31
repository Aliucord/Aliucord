package com.discord.api.message.embed;

@SuppressWarnings("unused")
public final class EmbedThumbnail {
    private final Integer height;
    private final String proxyUrl;
    private final String url;
    private final Integer width;

    public EmbedThumbnail(String url, String proxyUrl, Integer height, Integer width) {
        this.url = url;
        this.proxyUrl = proxyUrl;
        this.height = height;
        this.width = width;
    }

    // getHeight
    public final Integer a() { return height; }
    // getProxyUrl
    public final String b() { return proxyUrl; }
    // getUrl
    public final String c() { return url; }
    // getWidth
    public final Integer d() { return width; }
}
