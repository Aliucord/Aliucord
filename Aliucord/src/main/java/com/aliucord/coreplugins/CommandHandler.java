/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aliucord.CollectionUtils;
import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.api.CommandsAPI;
import com.aliucord.api.PatcherAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PrePatchRes;
import com.discord.api.commands.Application;
import com.discord.models.commands.ApplicationCommand;
import com.discord.models.commands.ApplicationSubCommand;
import com.discord.models.commands.RemoteApplicationCommand;
import com.discord.models.domain.ModelMessage;
import com.discord.stores.StoreStream;
import com.discord.widgets.chat.input.*;
import com.discord.widgets.chat.list.entries.MessageEntry;
import com.discord.widgets.chat.list.sheet.WidgetApplicationCommandBottomSheetViewModel;

import java.util.*;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public class CommandHandler extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() { return new Manifest(); }

    private static final class Classes {
        private static final String storeBuiltInCommands = "com.discord.stores.BuiltInCommands";
        private static final String storeApplicationCommands = "com.discord.stores.StoreApplicationCommands";
        private static final String storeLocalMessagesHolder = "com.discord.stores.StoreLocalMessagesHolder";
        private static final String configureSendListeners = "com.discord.widgets.chat.input.WidgetChatInput$configureSendListeners$2";

        private static final String commandItem = "com.discord.widgets.chat.input.WidgetChatInputCommandsAdapter$Item";
        private static final String modelMessage = "com.discord.models.domain.ModelMessage";
        private static final String adapterItemMessage = "com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage";
        private static final String commandBottomSheetViewModel = "com.discord.widgets.chat.list.sheet.WidgetApplicationCommandBottomSheetViewModel";
    }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(Classes.storeBuiltInCommands, Collections.singletonList("getBuiltInCommands"));
        map.put(Classes.storeApplicationCommands, Arrays.asList("getApplications", "getApplicationMap", "getQueryCommands", "handleGuildApplicationsUpdate"));
        map.put(Classes.storeLocalMessagesHolder, Collections.singletonList("getFlattenedMessages"));
        map.put(Classes.configureSendListeners, Collections.singletonList("*"));

        map.put(Classes.commandItem, Collections.singletonList("onConfigure"));
        map.put(Classes.modelMessage, Collections.singletonList("isLocalApplicationCommand"));
        map.put(Classes.adapterItemMessage, Arrays.asList("configureInteractionMessage", "processMessageText"));
        map.put(Classes.commandBottomSheetViewModel, Collections.singletonList("requestInteractionData"));
        return map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Context context) {
        Logger logger = new Logger("CommandHandler");

        Patcher.addPatch(Classes.storeBuiltInCommands, "getBuiltInCommands", (_this, args, ret) -> {
            List<ApplicationCommand> list = (List<ApplicationCommand>) ret;
            Collection<RemoteApplicationCommand> addList = CommandsAPI.commands.values();
            if (list.containsAll(addList)) return ret;
            if (!(list instanceof ArrayList)) list = new ArrayList<>(list);
            list.removeAll(addList);
            list.addAll(addList);
            return list;
        });

        Patcher.addPatch(Classes.storeApplicationCommands, "getApplications", (_this, args, ret) -> {
            List<Application> list = (List<Application>) ret;
            if (list == null || list.contains(CommandsAPI.getAliucordApplication())) return ret;
            if (!(list instanceof ArrayList)) list = new ArrayList<>(list);
            list.add(CommandsAPI.getAliucordApplication());
            return list;
        });

        Patcher.addPatch(Classes.storeApplicationCommands, "getApplicationMap", (_this, args, ret) -> {
            Map<Long, Application> map = (Map<Long, Application>) ret;
            if (map == null || map.containsKey(CommandsAPI.ALIUCORD_APP_ID)) return ret;
            if (!(map instanceof LinkedHashMap)) map = new LinkedHashMap<>(map);
            map.put(CommandsAPI.ALIUCORD_APP_ID, CommandsAPI.getAliucordApplication());
            return map;
        });

        Patcher.addPrePatch(Classes.storeApplicationCommands, "handleGuildApplicationsUpdate", (_this, args) -> {
            List<Application> list = (List<Application>) args.get(0);
            if (list == null || list.contains(CommandsAPI.getAliucordApplication())) return null;
            if (!(list instanceof ArrayList)) list = new ArrayList<>(list);
            list.add(CommandsAPI.getAliucordApplication());
            return null;
        });

        Patcher.addPatch(Classes.storeLocalMessagesHolder, "getFlattenedMessages", (_this, args, ret) -> {
            List<ModelMessage> list = (List<ModelMessage>) ret;
            CollectionUtils.removeIf(list, m -> {
                boolean r = m.getAuthor().f() == -1 || m.getAuthor().f() == 0;
                if (r) StoreStream.getMessages().deleteMessage(m);
                return r;
            });
            return list;
        });

        // needed to reimplement this to:
        // 1. don't send command result if not needed
        // 2. fully support arguments in built-in subcommands
        // 3. clear input after executing command
        Patcher.addPrePatch(Classes.configureSendListeners, "invoke", (__this, args) -> {
            ApplicationCommandData data = (ApplicationCommandData) args.get(1);
            ApplicationCommand command;
            if (data != null && (command = data.getApplicationCommand()) != null && command instanceof RemoteApplicationCommand && command.getBuiltIn()) {
                List<ApplicationCommandValue> values = data.getValues();
                if (values != null) {
                    LinkedHashMap<String, Object> commandArgs = new LinkedHashMap<>(values.size());
                    addValues(commandArgs, values);
                    Function1<Map<String, ?>, String> execute = command.getExecute();
                    if (execute != null) {
                        WidgetChatInput$configureSendListeners$2 _this = (WidgetChatInput$configureSendListeners$2) __this;
                        commandArgs.put("__this", _this);
                        commandArgs.put("__args", args);

                        command.getExecute().invoke(commandArgs);
                        return new PrePatchRes(true);
                    }
                }
            }
            return null;
        });

        // display plugin name instead of "Aliucord" in command autocomplete
        Patcher.addPatch(Classes.commandItem, "onConfigure", (_this, args, ret) -> {
            WidgetChatInputCommandsModel model = (WidgetChatInputCommandsModel) args.get(1);
            ApplicationCommand command = model.getCommand();
            String plugin = null;
            if (command instanceof ApplicationSubCommand && ((ApplicationSubCommand) command).getRootCommand().getBuiltIn()) {
                plugin = CommandsAPI.commandsAndPlugins.get(((ApplicationSubCommand) command).getRootCommand().getName());
            } else if (command instanceof RemoteApplicationCommand && command.getBuiltIn()) {
                plugin = CommandsAPI.commandsAndPlugins.get(command.getName());
            }
            if (plugin == null) return ret;
            TextView itemNameRight = ((WidgetChatInputCommandsAdapter.Item) _this).itemView
                    .findViewById(Utils.getResId("chat_input_item_name_right", "id"));
            itemNameRight.setText(plugin.toUpperCase());
            return ret;
        });

        Patcher.addPrePatch(Classes.adapterItemMessage, "configureInteractionMessage", (_this, args) -> {
            ModelMessage message = ((MessageEntry) args.get(0)).getMessage();
            if (message != null && message.isLoading() && !message.isLocalApplicationCommand())
                unpatch = PatcherAPI.addPrePatch(Classes.modelMessage, "isLocalApplicationCommand", (_this1, args1) -> new PrePatchRes(true));
            return null;
        });
        Patcher.addPatch(Classes.adapterItemMessage, "configureInteractionMessage", (_this, args, ret) -> {
            if (unpatch != null) {
                unpatch.run();
                unpatch = null;
            }
            return ret;
        });

        // don't mark Aliucord command messages as sending
        Patcher.addPatch(Classes.adapterItemMessage, "processMessageText", (_this, args, ret) -> {
            if (args.size() < 2) return ret;
            ModelMessage message = ((MessageEntry) args.get(1)).getMessage();
            if (message != null && message.getType() == ModelMessage.TYPE_LOCAL && message.getAuthor().f() == -1) {
                TextView textView = (TextView) args.get(0);
                if (textView.getAlpha() != 1.0f) textView.setAlpha(1.0f);
            }
            return ret;
        });


        Patcher.addPrePatch(Classes.commandBottomSheetViewModel, "requestInteractionData", (__this, args) -> {
            WidgetApplicationCommandBottomSheetViewModel _this = (WidgetApplicationCommandBottomSheetViewModel) __this;
            if (_this.getApplicationId() != -1) return null;
            WidgetApplicationCommandBottomSheetViewModel.StoreState state = CommandsAPI.interactionsStore.get(_this.getInteractionId());
            if (state != null) WidgetApplicationCommandBottomSheetViewModel.access$handleStoreState(_this, state);
            return new PrePatchRes(null);
        });
    }

    private void addValues(LinkedHashMap<String, Object> map, List<ApplicationCommandValue> values) {
        for (ApplicationCommandValue v : values) {
            String name = v.getName();
            Object value = v.getValue();
            List<ApplicationCommandValue> options = v.getOptions();
            if (value == null && options != null) {
                LinkedHashMap<String, Object> optionsMap = new LinkedHashMap<>();
                addValues(optionsMap, options);
                map.put(name, optionsMap);
            } else map.put(name, value);
        }
    }

    @Override
    public void start(Context context) {}

    @Override
    public void stop(Context context) {}

    public static Runnable unpatch;
}
