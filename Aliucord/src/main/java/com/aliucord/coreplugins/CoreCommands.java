/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.content.Context;
import android.text.TextUtils;

import com.aliucord.PluginManager;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.models.commands.ApplicationCommandOption;

import java.util.Collections;
import java.util.Set;

final class CoreCommands extends Plugin {
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

        commands.registerCommand(
            "plugins",
            "Lists installed plugins",
            Collections.singletonList(new ApplicationCommandOption(ApplicationCommandType.BOOLEAN, "send", "Whether the result should be visible for everyone", null, false, false, null, null, null)),
            ctx -> {
                boolean send = ctx.getBoolOrDefault("send", false);
                Set<String> plugins = PluginManager.plugins.keySet();
                if (plugins.isEmpty()) return new CommandsAPI.CommandResult("No plugins installed", null, send);
                String content = String.format("Installed Plugins (%s): %s", plugins.size(), TextUtils.join(", ", plugins));
                return new CommandsAPI.CommandResult(content, null, send);
            }
        );
    }

    @Override
    public void stop(Context context) {}
}
