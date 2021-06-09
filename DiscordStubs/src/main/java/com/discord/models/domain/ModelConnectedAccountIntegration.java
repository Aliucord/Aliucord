package com.discord.models.domain;

@SuppressWarnings("unused")
public final class ModelConnectedAccountIntegration {
    public static final String TWITCH_URL_PREFIX = "twitch.tv/";
    public static final String TYPE_TWITCH = "twitch";
    public static final String TYPE_YOUTUBE = "youtube";
    public static final String YOUTUBE_URL_PREFIX = "youtube.com/channel/";
    public static final class Parser  {
        public static final Parser INSTANCE = null;
    }
    /*
    public ModelConnectedAccountIntegration(
            String id,
            String type,
            ModelConnectedIntegrationAccount account,
            ModelConnectedIntegrationGuild guild) { }
     */
    public final String getDisplayName() { return null; }
    // public final ModelConnectedIntegrationAccount getAccount() { return null; }
    // public final ModelConnectedIntegrationGuild getGuild() { return null; }
    public final String getId() { return null; }
    public final String getType() { return null; }
}
