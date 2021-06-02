/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api;

import android.os.Handler;
import android.os.Looper;

import com.aliucord.Main;
import com.aliucord.Utils;
import com.aliucord.utils.ReflectUtils;
import com.discord.api.commands.ApplicationCommandData;
import com.discord.api.message.embed.MessageEmbed;
import com.discord.models.domain.ModelMessage;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.api.commands.Application;
import com.discord.models.commands.ApplicationCommand;
import com.discord.models.commands.ApplicationCommandOption;
import com.discord.models.commands.RemoteApplicationCommand;
import com.discord.models.domain.NonceGenerator;
import com.discord.models.user.User;
import com.discord.stores.StoreApplicationInteractions;
import com.discord.stores.StoreMessages;
import com.discord.stores.StoreStream;
import com.discord.utilities.SnowflakeUtils;
import com.discord.utilities.time.Clock;
import com.discord.utilities.time.ClockFactory;
import com.discord.utilities.user.UserUtils;
import com.discord.widgets.chat.MessageContent;
import com.discord.widgets.chat.input.ChatInputViewModel;
import com.discord.widgets.chat.input.WidgetChatInput;
import com.discord.widgets.chat.input.WidgetChatInput$configureSendListeners$2;
import com.discord.widgets.chat.list.sheet.WidgetApplicationCommandBottomSheetViewModel;
import com.lytefast.flexinput.R$g;
import com.lytefast.flexinput.model.Attachment;

import java.lang.reflect.Field;
import java.util.*;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@SuppressWarnings({"unchecked", "unused"})
public class CommandsAPI {
    public static class CommandResult {
        public String content;
        public List<MessageEmbed> embeds;
        public boolean send;
        public String username;
        public String avatarUrl;

        public CommandResult() { this(null); }
        public CommandResult(String content) {
            this(content, null, true);
        }
        public CommandResult(String content, List<MessageEmbed> embeds, boolean send) {
            this.content = content;
            this.embeds = embeds;
            this.send = send;
        }
        public CommandResult(String content, List<MessageEmbed> embeds, boolean send, String username) {
            this.content = content;
            this.embeds = embeds;
            this.username = username;
            this.send = send;
        }
        public CommandResult(String content, List<MessageEmbed> embeds, boolean send, String username, String avatarUrl) {
            this.content = content;
            this.embeds = embeds;
            this.username = username;
            this.avatarUrl = avatarUrl;
            this.send = send;
        }
    }

    public static final long ALIUCORD_APP_ID = generateId();
    public static final String DONT_SEND_RESULT = "{ALIUCORD_COMMAND}";
    private static final Application aliucordApplication = new Application(ALIUCORD_APP_ID, "Aliucord", null, 0, null, true);
    public static Map<String, RemoteApplicationCommand> commands = new HashMap<>();
    public static Map<String, String> commandsAndPlugins = new HashMap<>();
    public static Map<Long, WidgetApplicationCommandBottomSheetViewModel.StoreState> interactionsStore = new HashMap<>();
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
        RemoteApplicationCommand command = new RemoteApplicationCommand(generateIdString(), ALIUCORD_APP_ID, name, description, options, null, null, null, null, args -> {
            Clock clock = ClockFactory.get();
            long id = NonceGenerator.computeNonce(clock);
            long channelId = StoreStream.getChannelsSelected().getId();
            User me = StoreStream.getUsers().getMe();
            ModelMessage message = ModelMessage.createLocalApplicationCommandMessage(
                    id, name, channelId, UserUtils.INSTANCE.synthesizeApiUser(me), Utils.buildClyde(null, null), false, true, id, clock);
            Class<ModelMessage> c = ModelMessage.class;
            try {
                ReflectUtils.setField(c, message, "flags", 192L, true);
                ReflectUtils.setField(c, message, "type", ModelMessage.TYPE_LOCAL, true);
            } catch (Throwable ignored) {}
            StoreMessages storeMessages = StoreStream.getMessages();
            StoreMessages.access$handleLocalMessageCreate(storeMessages, message);

            WidgetChatInput$configureSendListeners$2 _this = (WidgetChatInput$configureSendListeners$2) args.get("__this");
            ArrayList<Object> _args = (ArrayList<Object>) args.get("__args");
            args.remove("__this");
            args.remove("__args");

            if (_this == null || _args == null) return null;
            MessageContent content = _this.$chatInput.getMatchedContentWithMetaData();
            WidgetChatInput.clearInput$default(_this.this$0, false, true, 0, null);

            new Thread(() -> {
                CommandResult res = execute.invoke(args);
                if (!res.send) {
                    // TODO: add arguments
                    long guildId = StoreStream.getChannels().getChannel(channelId).e();
                    interactionsStore.put(id, new WidgetApplicationCommandBottomSheetViewModel.StoreState(
                            me,
                            guildId == 0 ? null : StoreStream.getGuilds().getMembers().get(guildId).get(me.getId()),
                            new StoreApplicationInteractions.State.Loaded(new ApplicationCommandData("", "", "", name, Collections.emptyList())),
                            CommandsAPI.getAliucordApplication(),
                            Collections.emptySet(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                            Collections.emptyMap()
                    ));
                    try {
                        ReflectUtils.setField(c, message, "content", res.content, true);
                        ReflectUtils.setField(c, message, "embeds", res.embeds, true);
                        ReflectUtils.setField(c, message, "flags", 64L, true);
                        ReflectUtils.setField(c, message, "author", Utils.buildClyde(res.username, res.avatarUrl), true);
                        Utils.rerenderChat(); // TODO: figure out how to rerender single message
                    } catch (Throwable ignored) {}
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> ChatInputViewModel.sendMessage$default(
                            WidgetChatInput.access$getViewModel$p(_this.this$0),
                            _this.$context,
                            _this.$messageManager,
                            new MessageContent(res.content, content != null ? content.getMentionedUsers() : Collections.emptyList()),
                            (List<? extends Attachment<?>>) _args.get(0),
                            false,
                            (Function1<? super Boolean, Unit>) _args.get(2),
                            16,
                            null
                    ));
                    storeMessages.deleteMessage(message);
                }
            }).start();
            return null;
        });
        try {
            ReflectUtils.setField(ApplicationCommand.class, command, "builtIn", true, true);
        } catch (Throwable e) { Main.logger.error(e); }
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
