package com.discord.models.commands;

import com.discord.models.commands.ApplicationCommandOption;

import java.util.List;
import java.util.Map;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;

@SuppressWarnings("unused")
public final class RemoteApplicationCommand extends com.discord.models.commands.ApplicationCommand {
    public RemoteApplicationCommand(
            String id,
            long appId,
            String name,
            String description,
            List<ApplicationCommandOption> options,
            long version,
            Function1<? super Map<String, ?>, String> execute,
            int i,
            DefaultConstructorMarker defaultConstructorMarker
    ) {
        this(id, appId, name, (i & 8) != 0 ? null : description, options, version, (i & 64) != 0 ? null : execute);
    }

    public RemoteApplicationCommand(
            String id,
            long appId,
            String name,
            String description,
            List<ApplicationCommandOption> options,
            long version,
            Function1<? super Map<String, ?>, String> execute
    ) {
        super(id, appId, name, description, null, options, false, version, execute, null);
    }
}
