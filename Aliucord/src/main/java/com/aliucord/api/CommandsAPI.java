package com.aliucord.api;

import com.aliucord.Main;
import com.aliucord.Utils;
import com.discord.models.domain.ModelMessageEmbed;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.api.commands.Application;
import com.discord.models.commands.ApplicationCommand;
import com.discord.models.commands.ApplicationCommandOption;
import com.discord.models.commands.RemoteApplicationCommand;
import com.discord.stores.StoreStream;
import com.discord.utilities.SnowflakeUtils;
import com.lytefast.flexinput.R$g;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public class CommandsAPI {
    public static class CommandResult {
        public String content;
        public List<ModelMessageEmbed> embeds;
        public boolean send;

        public CommandResult() { this(null); }
        public CommandResult(String content) {
            this(content, null, true);
        }
        public CommandResult(String content, List<ModelMessageEmbed> embeds, boolean send) {
            this.content = content;
            this.embeds = embeds;
            this.send = send;
        }
    }

    public static final long ALIUCORD_APP_ID = generateId();
    public static final String DONT_SEND_RESULT = "{ALIUCORD_COMMAND}";
    private static final Application aliucordApplication = new Application(ALIUCORD_APP_ID, "Aliucord", null, 0, null, true);
    public static Map<String, RemoteApplicationCommand> commands = new HashMap<>();
    public static Map<String, String> commandsAndPlugins = new HashMap<>();
    public static ApplicationCommandOption messageOption
            = new ApplicationCommandOption(ApplicationCommandType.STRING, "message", null, R$g.command_shrug_message_description, false, false, null, null);
    public static ApplicationCommandOption requiredMessageOption
            = new ApplicationCommandOption(ApplicationCommandType.STRING, "message", null, R$g.command_shrug_message_description, true, false, null, null);

    private static void _registerCommand(
            String name,
            String description,
            List<ApplicationCommandOption> options,
            Function1<? super Map<String, ?>, CommandResult> execute
    ) {
        RemoteApplicationCommand command = new RemoteApplicationCommand(generateIdString(), ALIUCORD_APP_ID, name, description, options, null, null, args -> {
            CommandResult res = execute.invoke(args);
            if (!res.send) {
                Utils.createClydeMessage(res.content, StoreStream.getChannelsSelected().getId(), res.embeds);
                return DONT_SEND_RESULT;
            }
            return res.content == null ? DONT_SEND_RESULT : res.content;
        });
        try {
            Field builtIn = ApplicationCommand.class.getDeclaredField("builtIn");
            builtIn.setAccessible(true);
            builtIn.set(command, true);
        } catch (Exception e) { Main.logger.error(e); }
        commands.put(name, command);
        updateCommandCount();
    }

    private static void _unregisterCommand(String name) {
        commands.remove(name);
        updateCommandCount();
    }

    public static Application getAliucordApplication() {
        updateCommandCount();
        return aliucordApplication;
    }

    private static void updateCommandCount() {
        if (aliucordApplication.c() != commands.size()) {
            try {
                Field commandsField = Application.class.getDeclaredField("commandCount");
                commandsField.setAccessible(true);
                commandsField.setInt(aliucordApplication, commands.size());
            } catch (Exception ignored) {}
        }
    }

    public static long generateId() {
        return -SnowflakeUtils.fromTimestamp(System.currentTimeMillis() * 100);
    }
    public static String generateIdString() {
        return String.valueOf(generateId());
    }

    public final String pluginName;
    public final List<String> pluginCommands = new ArrayList<>();
    public CommandsAPI(String plugin) {
        pluginName = plugin;
    }

    public void registerCommand(
            String name,
            String description,
            List<ApplicationCommandOption> options,
            Function1<? super Map<String, ?>, CommandResult> execute
    ) {
        _registerCommand(name, description, options, execute);
        commandsAndPlugins.put(name, pluginName);
        pluginCommands.add(name);
    }

    public void unregisterCommand(String name) {
        _unregisterCommand(name);
        commandsAndPlugins.remove(name);
        pluginCommands.remove(name);
    }

    public void unregisterAll() {
        for (String name : pluginCommands) {
            _unregisterCommand(name);
            commandsAndPlugins.remove(name);
        }
        pluginCommands.clear();
    }
}
