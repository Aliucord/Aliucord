/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aliucord.CollectionUtils;
import com.aliucord.Utils;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.patcher.PinePrePatchFn;
import com.aliucord.wrappers.messages.MessageWrapper;
import com.discord.api.message.Message;
import com.discord.api.message.MessageTypes;
import com.discord.models.commands.*;
import com.discord.models.user.CoreUser;
import com.discord.stores.StoreApplicationCommands;
import com.discord.stores.StoreStream;
import com.discord.utilities.message.MessageUtils;
import com.discord.utilities.view.text.SimpleDraweeSpanTextView;
import com.discord.widgets.chat.input.*;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.entries.MessageEntry;
import com.discord.widgets.chat.list.sheet.WidgetApplicationCommandBottomSheetViewModel;

import java.util.*;

import kotlin.jvm.functions.Function1;

public final class CommandHandler extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() { return new Manifest(); }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Context context) throws Throwable {
        Patcher.addPatch("com.discord.stores.BuiltInCommands", "getBuiltInCommands", new Class<?>[0], new PinePatchFn(callFrame -> {
            List<ApplicationCommand> list = (List<ApplicationCommand>) callFrame.getResult();
            Collection<RemoteApplicationCommand> addList = CommandsAPI.commands.values();
            if (list.containsAll(addList)) return;
            if (!(list instanceof ArrayList)) list = new ArrayList<>(list);
            list.removeAll(addList);
            list.addAll(addList);
            callFrame.setResult(list);
        }));

        Patcher.addPatch(StoreApplicationCommands.class.getDeclaredMethod("getApplications"), new PinePatchFn(callFrame -> {
            List<Application> list = (List<Application>) callFrame.getResult();
            if (list == null || list.contains(CommandsAPI.getAliucordApplication())) return;
            if (!(list instanceof ArrayList)) list = new ArrayList<>(list);
            list.add(CommandsAPI.getAliucordApplication());
            callFrame.setResult(list);
        }));

        Patcher.addPatch(StoreApplicationCommands.class, "getApplicationMap", new Class<?>[0], new PinePatchFn(callFrame -> {
            Map<Long, Application> map = (Map<Long, Application>) callFrame.getResult();
            if (map == null || map.containsKey(CommandsAPI.ALIUCORD_APP_ID)) return;
            if (!(map instanceof LinkedHashMap)) map = new LinkedHashMap<>(map);
            map.put(CommandsAPI.ALIUCORD_APP_ID, CommandsAPI.getAliucordApplication());
            callFrame.setResult(map);
        }));

        Patcher.addPatch(StoreApplicationCommands.class, "handleGuildApplicationsUpdate", new Class<?>[]{ List.class }, new PinePrePatchFn(callFrame -> {
            List<Application> list = (List<Application>) callFrame.args[0];
            if (list == null || list.contains(CommandsAPI.getAliucordApplication())) return;
            if (!(list instanceof ArrayList)) {
                list = new ArrayList<>(list);
                callFrame.args[0] = list;
            }
            list.add(CommandsAPI.getAliucordApplication());
        }));

        Patcher.addPatch("com.discord.stores.StoreLocalMessagesHolder", "getFlattenedMessages", new Class<?>[0], new PinePatchFn(callFrame -> {
            List<Message> list = (List<Message>) callFrame.getResult();
            CollectionUtils.removeIf(list, m -> {
                CoreUser author = new CoreUser(MessageWrapper.getAuthor(m));
                boolean r = author.getId() == -1 || author.getId() == 0;
                if (r) StoreStream.getMessages().deleteMessage(m);
                return r;
            });
            callFrame.setResult(list);
        }));

        // needed to reimplement this to:
        // 1. don't send command result if not needed
        // 2. fully support arguments in built-in subcommands
        // 3. clear input after executing command
        Patcher.addPatch(WidgetChatInput$configureSendListeners$2.class.getDeclaredMethod("invoke", List.class, ApplicationCommandData.class, Function1.class), new PinePrePatchFn(callFrame -> {
            ApplicationCommandData data = (ApplicationCommandData) callFrame.args[1];
            ApplicationCommand command;
            if (data != null && (command = data.getApplicationCommand()) != null && command instanceof RemoteApplicationCommand && command.getBuiltIn()) {
                List<ApplicationCommandValue> values = data.getValues();
                if (values != null) {
                    LinkedHashMap<String, Object> commandArgs = new LinkedHashMap<>(values.size());
                    addValues(commandArgs, values);
                    Function1<Map<String, ?>, String> execute = command.getExecute();
                    if (execute != null) {
                        WidgetChatInput$configureSendListeners$2 _this = (WidgetChatInput$configureSendListeners$2) callFrame.thisObject;
                        commandArgs.put("__this", _this);
                        commandArgs.put("__args", callFrame.args);

                        command.getExecute().invoke(commandArgs);
                        callFrame.setResult(true);
                    }
                }
            }
        }));

        // display plugin name instead of "Aliucord" in command autocomplete
        Patcher.addPatch(WidgetChatInputCommandsAdapter.Item.class.getDeclaredMethod("onConfigure", int.class, WidgetChatInputCommandsModel.class),
            new PinePatchFn(callFrame -> {
                WidgetChatInputCommandsModel model = (WidgetChatInputCommandsModel) callFrame.args[1];
                ApplicationCommand command = model.getCommand();
                String plugin = null;
                if (command instanceof ApplicationSubCommand && ((ApplicationSubCommand) command).getRootCommand().getBuiltIn()) {
                    plugin = CommandsAPI.commandsAndPlugins.get(((ApplicationSubCommand) command).getRootCommand().getName());
                } else if (command instanceof RemoteApplicationCommand && command.getBuiltIn()) {
                    plugin = CommandsAPI.commandsAndPlugins.get(command.getName());
                }
                if (plugin == null) return;
                TextView itemNameRight = ((WidgetChatInputCommandsAdapter.Item) callFrame.thisObject).itemView
                    .findViewById(Utils.getResId("chat_input_item_name_right", "id"));
                itemNameRight.setText(plugin.toUpperCase());
            })
        );

        Patcher.addPatch(MessageUtils.class.getDeclaredMethod("isLocalApplicationCommand", Message.class), new PinePrePatchFn(callFrame -> {
            Message message;
            @SuppressWarnings("WrapperTypeMayBePrimitive") Integer type;
            //noinspection ConstantConditions
            if ((message = (Message) callFrame.args[0]) == null || (type = MessageWrapper.getType(message)) == null) return;
            MessageUtils messageUtils = (MessageUtils) callFrame.thisObject;
            if (messageUtils.isLoading(message) && type != MessageTypes.LOCAL_APPLICATION_COMMAND && type != MessageTypes.LOCAL_APPLICATION_COMMAND_SEND_FAILED)
                callFrame.setResult(true);
        }));

        // don't mark Aliucord command messages as sending
        Patcher.addPatch(WidgetChatListAdapterItemMessage.class, "processMessageText", new Class<?>[]{ SimpleDraweeSpanTextView.class, MessageEntry.class },
            new PinePatchFn(callFrame -> {
                Message message = ((MessageEntry) callFrame.args[1]).getMessage();
                if (message != null && MessageWrapper.getType(message) == MessageTypes.LOCAL && new CoreUser(MessageWrapper.getAuthor(message)).getId() == -1) {
                    TextView textView = (TextView) callFrame.args[0];
                    if (textView.getAlpha() != 1.0f) textView.setAlpha(1.0f);
                }
            })
        );


        Patcher.addPatch(WidgetApplicationCommandBottomSheetViewModel.class, "requestInteractionData", new Class<?>[0], new PinePrePatchFn(callFrame -> {
            WidgetApplicationCommandBottomSheetViewModel _this = (WidgetApplicationCommandBottomSheetViewModel) callFrame.thisObject;
            if (_this.getApplicationId() != -1) return;
            WidgetApplicationCommandBottomSheetViewModel.StoreState state = CommandsAPI.interactionsStore.get(_this.getInteractionId());
            if (state != null) WidgetApplicationCommandBottomSheetViewModel.access$handleStoreState(_this, state);
            callFrame.setResult(null);
        }));
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
}
