package com.discord.models.commands;

import java.util.List;
import java.util.Map;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;

@SuppressWarnings("unused")
public abstract class ApplicationCommand {
    private final boolean builtIn;

    public ApplicationCommand(
            String id,
            long appId,
            String name,
            String description,
            Integer descriptionRes,
            List<ApplicationCommandOption> options,
            boolean builtIn,
            long version,
            Function1<? super Map<String, ?>, String> execute,
            DefaultConstructorMarker defaultConstructorMarker
    ) {
        this.builtIn = builtIn;
    }

    public final long getApplicationId() { return 0; }
    public final boolean getBuiltIn() { return builtIn; }
    public final String getDescription() { return ""; }
    public final Integer getDescriptionRes() { return null; }
    public final String getId() { return ""; }
    public final String getName() { return ""; }
    public final Function1<Map<String, ?>, String> getExecute() { return null; }
    public final List<ApplicationCommandOption> getOptions() { return null; }
}
