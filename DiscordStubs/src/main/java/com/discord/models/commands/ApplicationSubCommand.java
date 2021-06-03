package com.discord.models.commands;

import com.discord.api.commands.ApplicationCommandPermission;

import java.util.List;

@SuppressWarnings("unused")
public final class ApplicationSubCommand extends ApplicationCommand {
    private final String parentGroupName;
    private final ApplicationCommand rootCommand;
    private final String subCommandName;

    public ApplicationSubCommand(
            ApplicationCommand rootCommand,
            String subCommandName,
            String parentGroupName,
            long appId,
            String name,
            String description,
            Integer descriptionRes,
            List<ApplicationCommandOption> options,
            Long guildId,
            Boolean defaultPermissions,
            List<ApplicationCommandPermission> permissions,
            String version
    ) {
        super(
                rootCommand.getId() + parentGroupName + name,
                appId,
                name,
                description,
                descriptionRes,
                options,
                false,
                guildId,
                version,
                defaultPermissions,
                permissions,
                null,
                576,
                null
        );
        this.rootCommand = rootCommand;
        this.subCommandName = subCommandName;
        this.parentGroupName = parentGroupName;
    }

    public final String getParentGroupName() {
        return this.parentGroupName;
    }

    public final ApplicationCommand getRootCommand() {
        return this.rootCommand;
    }

    public final String getSubCommandName() {
        return this.subCommandName;
    }
}
