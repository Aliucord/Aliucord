package com.discord.api.message.embed;

@SuppressWarnings("unused")
public enum EmbedType {
    ARTICLE("article"),
    IMAGE("image"),
    VIDEO("video"),
    TWEET("tweet"),
    LINK("link"),
    HTML("html"),
    FILE("file"),
    GIFV("gifv"),
    RICH("rich"),
    APPLICATION_NEWS("application_news"),
    UNKNOWN("unknown");

    private final String apiValue;

    EmbedType(String str) {
        apiValue = str;
    }

    public final String getApiValue() {
        return this.apiValue;
    }
}
