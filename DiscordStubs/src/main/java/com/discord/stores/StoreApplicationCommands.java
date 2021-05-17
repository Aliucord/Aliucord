package com.discord.stores;

import com.discord.api.commands.Application;
import com.discord.models.commands.ApplicationCommand;
import com.discord.stores.updates.ObservationDeck;

import java.util.List;

@SuppressWarnings("unused")
public class StoreApplicationCommands extends StoreV2 {
    public static String access$getQuery$p(StoreApplicationCommands instance) { return null; }
    public static List<ApplicationCommand> access$getQueryCommands$p(StoreApplicationCommands instance) { return null; }
    public static ObservationDeck.UpdateSource access$getQueryCommandsUpdate$cp() { return null; }
    public static void access$handleGuildApplicationsUpdate(StoreApplicationCommands instance, List<Application> applications) {}

    public final List<Application> getApplications() { return null; }
}
