package com.discord.models.commands;

import com.discord.api.commands.ApplicationCommandType;
import com.discord.api.commands.CommandChoice;

import java.util.List;

@SuppressWarnings("unused")
public final class ApplicationCommandOption {
    public ApplicationCommandOption(
            ApplicationCommandType type,
            String name,
            String description,
            Integer descriptionRes,
            boolean required,
            boolean def,
            List<CommandChoice> choices,
            List<ApplicationCommandOption> options
    ) {}

    public final String getName() { return ""; }
}
