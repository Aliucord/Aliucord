package com.discord.stores;

@SuppressWarnings("unused")
public final class StoreStream {
    public static StoreApplicationCommands getApplicationCommands() { return new StoreApplicationCommands(); }
    public static StoreAuthentication getAuthentication() { return new StoreAuthentication(); }
    public static StoreChannels getChannels() { return new StoreChannels(); }
    public static StoreChannelsSelected getChannelsSelected() { return new StoreChannelsSelected(); }
    public static StoreGatewayConnection getGatewaySocket() { return new StoreGatewayConnection(); }
    public static StoreGuilds getGuilds() { return new StoreGuilds(); }
    public static StoreGuildSelected getGuildSelected() { return new StoreGuildSelected(); }
    public static StoreMessages getMessages() { return new StoreMessages(); }
    public static StoreUser getUsers() { return new StoreUser(); }
    public static StoreUserSettings getUserSettings() { return new StoreUserSettings(); }

    public static StoreStream access$getCollector$cp() { return new StoreStream(); }

    public final StoreClientVersion getClientVersion$app_productionGoogleRelease() { return new StoreClientVersion(); }
}
