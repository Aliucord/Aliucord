package com.discord.models.commands;

import com.discord.api.commands.ApplicationCommandPermission;

import java.util.List;
import java.util.Map;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public final class RemoteApplicationCommand extends com.discord.models.commands.ApplicationCommand {
    public RemoteApplicationCommand(
            String id,
            long appId,
            String name,
            String description,
            List<ApplicationCommandOption> options,
            Long guildId,
            String version,
            Boolean defaultPermissions,
            List<ApplicationCommandPermission> permissions,
            Function1<? super Map<String, ?>, String> execute
    ) {
        super(
                id,
                appId,
                name,
                description,
                null,
                options,
                false,
                guildId,
                version,
                defaultPermissions,
                permissions,
                execute,
                80,
                null
        );
    }
}
