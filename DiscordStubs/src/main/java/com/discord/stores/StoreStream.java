package com.discord.stores;

import com.discord.models.domain.ModelMessage;

@SuppressWarnings({"unused"})
public final class StoreStream {
    public static StoreApplicationCommands getApplicationCommands() { return new StoreApplicationCommands(); }
    public static StoreAuthentication getAuthentication() { return new StoreAuthentication(); }
    public static StoreChannels getChannels() { return new StoreChannels(); }
    public static StoreChannelsSelected getChannelsSelected() { return new StoreChannelsSelected(); }
    public static StoreGatewayConnection getGatewaySocket() { return new StoreGatewayConnection(); }
    public static StoreGuilds getGuilds() { return new StoreGuilds(); }
    public static StoreGuildSelected getGuildSelected() { return new StoreGuildSelected(); }
    public static StoreMessages getMessages() { return new StoreMessages(); }
    public static StoreSearch getSearch() { return new StoreSearch(); }
    public static StoreUser getUsers() { return new StoreUser(); }
    public static StoreUserSettings getUserSettings() { return new StoreUserSettings(); }
    public static StoreUserSettingsSystem getUserSettingsSystem() { return new StoreUserSettingsSystem(); }
    public static StorePermissions getPermissions() { return new StorePermissions(); }

    public static void handleMessageUpdate(StoreStream instance, ModelMessage message) {}

    public static final Companion Companion = new Companion();
    public static final class Companion {
        public static StoreStream access$getCollector$p(StoreStream.Companion companion) { return new StoreStream(); }
    }
}
