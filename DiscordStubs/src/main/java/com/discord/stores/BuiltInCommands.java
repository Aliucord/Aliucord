package com.discord.stores;

import com.discord.models.commands.Application;
import com.discord.models.commands.ApplicationCommand;

import java.util.List;

@SuppressWarnings("unused")
public final class BuiltInCommands implements BuiltInCommandsProvider {
    @Override
    public Application getBuiltInApplication() { return null; }
    @Override
    public List<ApplicationCommand> getBuiltInCommands() { return null; }
}
