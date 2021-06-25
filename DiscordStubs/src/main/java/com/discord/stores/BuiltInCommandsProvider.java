package com.discord.stores;

import com.discord.models.commands.Application;
import com.discord.models.commands.ApplicationCommand;

import java.util.List;

@SuppressWarnings("unused")
public interface BuiltInCommandsProvider {
    Application getBuiltInApplication();
    List<ApplicationCommand> getBuiltInCommands();
}
