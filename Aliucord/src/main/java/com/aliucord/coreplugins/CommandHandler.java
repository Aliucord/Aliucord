package com.aliucord.coreplugins;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aliucord.CollectionUtils;
import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PrePatchRes;
import com.discord.databinding.WidgetChatListActionsBinding;
import com.discord.models.domain.ModelMessage;
import com.discord.api.commands.Application;
import com.discord.models.commands.ApplicationSubCommand;
import com.discord.models.commands.ApplicationCommand;
import com.discord.models.commands.RemoteApplicationCommand;
import com.discord.widgets.chat.MessageContent;
import com.discord.widgets.chat.input.ChatInputViewModel;
import com.discord.widgets.chat.input.WidgetChatInput;
import com.discord.widgets.chat.input.WidgetChatInput$configureSendListeners$2;
import com.discord.widgets.chat.input.ApplicationCommandData;
import com.discord.widgets.chat.input.ApplicationCommandValue;
import com.discord.widgets.chat.input.WidgetChatInputCommandsAdapter;
import com.discord.widgets.chat.input.WidgetChatInputCommandsModel;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.lytefast.flexinput.model.Attachment;

import java.lang.reflect.Method;
import java.util.*;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public class CommandHandler extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() { return new Manifest(); }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("com.discord.stores.BuiltInCommands", Collections.singletonList("getBuiltInCommands"));
        map.put("com.discord.stores.StoreApplicationCommands", Arrays.asList("getApplications", "getApplicationMap", "getQueryCommands", "handleGuildApplicationsUpdate"));
        map.put("com.discord.stores.StoreLocalMessagesHolder", Collections.singletonList("getFlattenedMessages"));
        map.put("com.discord.widgets.chat.input.WidgetChatInput$configureSendListeners$2", Collections.singletonList("*"));

//        map.put("com.discord.widgets.chat.list", Collections.singletonList("processMessageText"));
        map.put("com.discord.widgets.chat.list.actions.WidgetChatListActions", Collections.singletonList("configureUI"));
        map.put("com.discord.widgets.chat.input.WidgetChatInputCommandsAdapter$Item", Collections.singletonList("onConfigure"));
        return map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Context context) {
        Logger logger = new Logger("CommandHandler");

        Patcher.addPatch("com.discord.stores.BuiltInCommands", "getBuiltInCommands", (_this, args, ret) -> {
            List<ApplicationCommand> list = (List<ApplicationCommand>) ret;
            Collection<RemoteApplicationCommand> addList = CommandsAPI.commands.values();
            if (list.containsAll(addList)) return ret;
            if (!(list instanceof ArrayList)) list = new ArrayList<>(list);
            list.removeAll(addList);
            list.addAll(addList);
            return list;
        });

        String className = "com.discord.stores.StoreApplicationCommands";
        Patcher.addPatch(className, "getApplications", (_this, args, ret) -> {
            List<Application> list = (List<Application>) ret;
            if (list == null || list.contains(CommandsAPI.getAliucordApplication())) return ret;
            if (!(list instanceof ArrayList)) list = new ArrayList<>(list);
            list.add(CommandsAPI.getAliucordApplication());
            return list;
        });

        Patcher.addPatch(className, "getApplicationMap", (_this, args, ret) -> {
            Map<Long, Application> map = (Map<Long, Application>) ret;
            if (map == null || map.containsKey(CommandsAPI.ALIUCORD_APP_ID)) return ret;
            if (!(map instanceof LinkedHashMap)) map = new LinkedHashMap<>(map);
            map.put(CommandsAPI.ALIUCORD_APP_ID, CommandsAPI.getAliucordApplication());
            return map;
        });

        Patcher.addPrePatch(className, "handleGuildApplicationsUpdate", (_this, args) -> {
            List<Application> list = (List<Application>) args.get(0);
            if (list == null || list.contains(CommandsAPI.getAliucordApplication())) return new PrePatchRes(args);
            if (!(list instanceof ArrayList)) list = new ArrayList<>(list);
            list.add(CommandsAPI.getAliucordApplication());
            return new PrePatchRes(args);
        });

        Patcher.addPatch("com.discord.stores.StoreLocalMessagesHolder", "getFlattenedMessages", (_this, args, ret) -> {
            List<ModelMessage> list = (List<ModelMessage>) ret;
            CollectionUtils.removeIf(list, m -> m.getAuthor().f() == -1 || m.getAuthor().f() == 0);
            return list;
        });

        // needed to reimplement this to:
        // 1. don't send command result if not needed
        // 2. fully support arguments in built-in subcommands
        // 3. clear input after executing command
        Patcher.addPrePatch("com.discord.widgets.chat.input.WidgetChatInput$configureSendListeners$2", "invoke", (__this, args) -> {
            ApplicationCommandData data = (ApplicationCommandData) args.get(1);
            ApplicationCommand command;
            if (data != null && (command = data.getApplicationCommand()) != null && command instanceof RemoteApplicationCommand && command.getBuiltIn()) {
                List<ApplicationCommandValue> values = data.getValues();
                if (values != null) {
                    LinkedHashMap<String, Object> commandArgs = new LinkedHashMap<>(values.size());
                    addValues(commandArgs, values);
                    Function1<Map<String, ?>, String> execute = command.getExecute();
                    if (execute != null) {
                        String res = command.getExecute().invoke(commandArgs);
                        WidgetChatInput$configureSendListeners$2 _this = (WidgetChatInput$configureSendListeners$2) __this;
                        if (res.equals(CommandsAPI.DONT_SEND_RESULT)) {
                            WidgetChatInput.clearInput$default(_this.this$0, false, true, 0, null);
                            return new PrePatchRes(args, true);
                        }
                        MessageContent content = _this.$chatInput.getMatchedContentWithMetaData();
                        if (content != null) {
                            ChatInputViewModel.sendMessage$default(
                                    WidgetChatInput.access$getViewModel$p(_this.this$0),
                                    _this.$context,
                                    _this.$messageManager,
                                    new MessageContent(res, content.getMentionedUsers()),
                                    (List<? extends Attachment<?>>) args.get(0),
                                    false,
                                    (Function1<? super Boolean, Unit>) args.get(2),
                                    16,
                                    null
                            );
                            return new PrePatchRes(args, null);
                        }
                    }
                }
            }
            return new PrePatchRes(args);
        });

//        Patcher.addPatch("com.discord.widgets.chat.list", "processMessageText", (_this, args, ret) -> {
//            logger.debug(args.toString());
//            if (args.size() < 2) return ret;
//            ModelMessage message = ((MessageEntry) args.get(1)).getMessage();
//            if (message != null && message.getType() == ModelMessage.TYPE_LOCAL && message.getAuthor().getId() == ModelUser.CLYDE_BOT_USER_ID) {
//                TextView textView = (TextView) args.get(0);
//                if (textView.getAlpha() != 1.0f) textView.setAlpha(1.0f);
//            }
//            return ret;
//        });

        // always allow to delete local messages
        try {
            Method getBinding = WidgetChatListActions.class.getDeclaredMethod("getBinding");
            getBinding.setAccessible(true);

            Patcher.addPatch("com.discord.widgets.chat.list.actions.WidgetChatListActions", "configureUI", (_this, args, ret) -> {
                WidgetChatListActions.Model model = (WidgetChatListActions.Model) args.get(0);
                if (model == null || model.getMessage() == null) return ret;
                boolean local = model.getMessage().isLocal();
                if (local) try {
                    WidgetChatListActionsBinding binding = (WidgetChatListActionsBinding) getBinding.invoke(_this);
                    if (binding != null) binding.e.setVisibility(View.VISIBLE);
                } catch (Throwable ignored) {}
                return ret;
            });
        } catch (Throwable e) { logger.error(e); }

        Patcher.addPatch("com.discord.widgets.chat.input.WidgetChatInputCommandsAdapter$Item", "onConfigure", (_this, args, ret) -> {
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
