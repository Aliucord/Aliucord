package com.aliucord.coreplugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;

import java.util.Collections;

public class CoreCommands extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() { return new Manifest(); }

    @Override
    public void start(Context context) {
        commands.registerCommand(
                "echo",
                "Creates Clyde message",
                Collections.singletonList(CommandsAPI.requiredMessageOption),
                args -> new CommandsAPI.CommandResult((String) args.get("message"), null, false)
        );

        commands.registerCommand(
                "say",
                "Sends message",
                Collections.singletonList(CommandsAPI.requiredMessageOption),
                args -> new CommandsAPI.CommandResult((String) args.get("message"))
        );
    }

    @Override
    public void stop(Context context) {}
}
