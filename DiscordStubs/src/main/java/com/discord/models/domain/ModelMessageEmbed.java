package com.discord.models.domain;

import com.discord.api.message.embed.*;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("unused")
public class ModelMessageEmbed {
    public static final String APPLICATION_NEWS = "application_news";
    public static final String ARTICLE = "article";
    public static final String FILE = "file";
    public static final String GIFV = "gifv";
    public static final String HTML = "html";
    public static final String IMAGE = "image";
    public static final String LINK = "link";
    public static final String RICH = "rich";
    public static final String TWEET = "tweet";
    public static final String VIDEO = "video";

    private boolean attachment;
    private EmbedAuthor author;
    private Integer color;
    private String description;
    private List<EmbedField> fields;
    private EmbedFooter footer;
    private EmbedImage image;
    private EmbedProvider provider;
    private Long referenceId;
    private EmbedThumbnail thumbnail;
    private String timestamp;
    private String title;
    private String type;
    private String url;
    private EmbedVideo video;

    public boolean isAttachment() { return attachment; }
    public EmbedAuthor getAuthor() { return author; }
    public Integer getColor() { return color; }
    public String getDescription() { return description; }
    public List<EmbedField> getFields() { return fields; }
    public EmbedFooter getFooter() { return footer; }
    public EmbedImage getImage() { return image; }
    public EmbedProvider getProvider() { return provider; }
    public Long getReferenceId() { return referenceId; }
    public EmbedThumbnail getThumbnail() { return thumbnail; }
    public String getTimestamp() { return timestamp; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getUrl() { return url; }
    public EmbedVideo getVideo() { return video; }

    public void assignField(Model.JsonReader jsonReader) throws IOException { throw new IOException(); }
}
