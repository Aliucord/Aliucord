package com.discord.stores;

import com.discord.models.commands.ApplicationCommand;

import java.util.List;

public interface BuiltInCommandsProvider {
    List<ApplicationCommand> getBuiltInCommands();
}
