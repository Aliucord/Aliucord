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

    EmbedType(String str) { }

    public final String getApiValue() { return null; }
}
