package com.discord.models.commands;

import java.util.List;
import java.util.Map;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public final class BuiltInCommand extends ApplicationCommand {
    public BuiltInCommand(
            String id,
            long appId,
            String name,
            Integer descriptionRes,
            List<ApplicationCommandOption> options,
            Function1<? super Map<String, ?>, String> execute
    ) {
        super(id, appId, name, null, descriptionRes, options, true, null, null, execute, 392, null);
    }
}
