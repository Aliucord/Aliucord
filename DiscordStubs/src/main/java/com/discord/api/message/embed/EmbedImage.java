package com.discord.api.message.embed;

@SuppressWarnings("unused")
public final class EmbedImage {
    private final Integer height;
    private final String proxyUrl;
    private final String url;
    private final Integer width;

    public EmbedImage(String url, String proxyUrl, Integer width, Integer height) {
        this.url = url;
        this.proxyUrl = proxyUrl;
        this.width = width;
        this.height = height;
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
