package com.discord.widgets.chat.input.autocomplete;

import com.discord.models.commands.Application;
import com.discord.models.commands.ApplicationCommand;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class ApplicationCommandAutocompletable {
    private final Application app;
    private final ApplicationCommand command;

    public ApplicationCommandAutocompletable(Application application, ApplicationCommand applicationCommand) {
        app = application;
        command = applicationCommand;
    }

    public final Application getApplication() { return app; }
    public final ApplicationCommand getCommand() { return command; }
    public String getInputReplacement() { return ""; }
    public List<String> getInputTextMatchers() { return new ArrayList<>(); }
    public final List<String> getTextMatchers() { return new ArrayList<>(); }
    public LeadingIdentifier leadingIdentifier() { return LeadingIdentifier.APP_COMMAND; }
}
