package com.discord.models.domain;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("unused")
public class ModelMessageEmbed {
    public static class Field {
        private boolean inline;
        private String name;
        private String value;

        public String getName() { return name; }
        public String getValue() { return value; }
        public boolean isInline() { return inline; }
    }
    public static class Item {}

    public static final String RICH = "rich";

    private boolean attachment;
    private Item author;
    private Integer color;
    private String description;
    private List<Field> fields;
    private Item footer;
    private Item image;
    private Item provider;
    private Long referenceId;
    private Item thumbnail;
    private String timestamp;
    private String title;
    private String type;
    private String url;
    private Item video;

    public boolean isAttachment() { return attachment; }
    public Item getAuthor() { return author; }
    public Integer getColor() { return color; }
    public String getDescription() { return description; }
    public List<Field> getFields() { return fields; }
    public Item getFooter() { return footer; }
    public Item getImage() { return image; }
    public Item getProvider() { return provider; }
    public Long getReferenceId() { return referenceId; }
    public Item getThumbnail() { return thumbnail; }
    public String getTimestamp() { return timestamp; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getUrl() { return url; }
    public Item getVideo() { return video; }

    public void assignField(Model.JsonReader jsonReader) throws IOException { throw new IOException(); }
}
