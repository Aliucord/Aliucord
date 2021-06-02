package com.discord.api.message.embed;

import com.discord.api.utcdatetime.UtcDateTime;

import java.util.List;

@SuppressWarnings("unused")
public final class MessageEmbed {
    private EmbedAuthor author;
    private Integer color;
    private String description;
    private List<EmbedField> fields;
    private EmbedFooter footer;
    private EmbedImage image;
    private EmbedProvider provider;
    private EmbedThumbnail thumbnail;
    private UtcDateTime timestamp;
    private String title;
    private EmbedType type;
    private String url;
    private EmbedVideo video;

    // getAuthor
    public final EmbedAuthor a() { return author; }
    // getColor
    public final Integer b() { return color; }
    // getDescription
    public final String c() { return description; }
    // getFields
    public final List<EmbedField> d() { return fields; }
    // getFooter
    public final EmbedFooter e() { return footer; }
    // getImage
    public final EmbedImage f() { return image; }
    // getProvider
    public final EmbedProvider g() { return provider; }
    // getThumbnail
    public final EmbedThumbnail h() { return thumbnail; }
    // getTimestamp
    public final UtcDateTime i() { return timestamp; }
    // getTitle
    public final String j() { return title; }
    // getType
    public final EmbedType k() { return type; }
    // getUrl
    public final String l() { return url; }
    // getVideo
    public final EmbedVideo m() { return video; }
}
