package com.discord.models.domain;

public final class ModelMuteConfig {
    /*
    public static final class Parser implements Model.Parser<ModelMuteConfig> {
        public ModelMuteConfig parse(Model.JsonReader jsonReader) { return null; }
    }
    */
    public ModelMuteConfig(Long endTimeMs, String endTime) { }
    public static ModelMuteConfig parse(Model.JsonReader jsonReader) { return null; }

    public final String getEndTime() { return null; }
    public final Long getEndTimeMs() { return null; }
}
