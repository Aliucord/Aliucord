/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;

import java.util.Collections;

final class CoreCommands extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() { return new Manifest(); }

    @Override
    public void start(Context context) {
        commands.registerCommand(
                "echo",
                "Creates Clyde message",
                Collections.singletonList(CommandsAPI.requiredMessageOption),
                ctx -> new CommandsAPI.CommandResult(ctx.getRequiredString("message"), null, false)
        );

        commands.registerCommand(
                "say",
                "Sends message",
                Collections.singletonList(CommandsAPI.requiredMessageOption),
                ctx -> new CommandsAPI.CommandResult(ctx.getRequiredString("message"))
        );
    }

    @Override
    public void stop(Context context) {}
}
