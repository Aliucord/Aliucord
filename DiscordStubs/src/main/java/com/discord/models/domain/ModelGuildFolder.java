package com.discord.models.domain;

import java.util.List;

public final class ModelGuildFolder {
    /*
    public static final class Parser implements Model.Parser<ModelGuildFolder> {
        public static final Parser INSTANCE = null;
        public ModelGuildFolder parse(Model.JsonReader jsonReader) { return null; }
    }
    */

    public ModelGuildFolder(Long id, List<Long> guildIds, Integer color, String name) { }
    public final Integer getColor() { return null; }
    public final List<Long> getGuildIds() { return null; }
    public final Long getId() { return null; }
    public final String getName() { return null; }
}
