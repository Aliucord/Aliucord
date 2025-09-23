package com.aliucord.coreplugins;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aliucord.coreplugins.forwardedmessages.ForwardSourceChatEntry;
import com.aliucord.coreplugins.forwardedmessages.WidgetChatListAdapterItemForwardSource;
import com.aliucord.entities.CorePlugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.PreHook;
import com.aliucord.updater.ManagerBuild;
import com.aliucord.utils.ReflectUtils;
import com.discord.api.application.Application;
import com.discord.api.channel.Channel;
import com.discord.api.interaction.Interaction;
import com.discord.api.message.*;
import com.discord.api.message.activity.MessageActivity;
import com.discord.api.message.allowedmentions.MessageAllowedMentions;
import com.discord.api.message.call.MessageCall;
import com.discord.api.message.role_subscription.RoleSubscriptionData;
import com.discord.api.user.User;
import com.discord.api.utcdatetime.UtcDateTime;
import com.discord.stores.*;
import com.discord.utilities.captcha.CaptchaHelper;
import com.discord.utilities.embed.InviteEmbedModel;
import com.discord.utilities.permissions.PermissionUtils;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.entries.ChatListEntry;
import com.discord.widgets.chat.list.entries.ReactionsEntry;
import com.discord.widgets.chat.list.model.WidgetChatListModelMessages;

import java.lang.reflect.Field;
import java.util.*;

public class ForwardedMessages extends CorePlugin {
    private static Field f_apiMessage_messageSnapshots;
    private static Field f_modelMessage_messageSnapshots;

    public ForwardedMessages() {
        super(new Manifest("ForwardedMessages"));
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public Object writeSnapshotFields(Object source, Object destination, boolean isApiMessage) throws NoSuchFieldException, IllegalAccessException {
        // We only ever call this method on objects that have this field
        var snapshots = (ArrayList<MessageSnapshot>) (isApiMessage ? f_apiMessage_messageSnapshots : f_modelMessage_messageSnapshots).get(source);

        if (snapshots == null || snapshots.isEmpty()) return destination;

        Message messageSnapshot = snapshots.get(0).message;
        assert messageSnapshot != null; // We can assume that if we're given a snapshot that its message field is present

        ReflectUtils.setField(destination, "messageSnapshots", snapshots);
        ReflectUtils.setField(destination, "embeds", messageSnapshot.k());
        ReflectUtils.setField(destination, "content", messageSnapshot.i());
        ReflectUtils.setField(destination, "attachments", messageSnapshot.d());
        ReflectUtils.setField(destination, "stickerItems", messageSnapshot.A());
        return destination;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void start(Context context) throws Throwable {
        if (!ManagerBuild.hasInjector("2.1.0") || !ManagerBuild.hasPatches("1.1.0")) {
            logger.warn("Base app outdated, cannot enable ForwardedMessages");
            return;
        }

        // Cache reflection since this is used in a performance-sensitive areas
        f_apiMessage_messageSnapshots = Message.class.getDeclaredField("messageSnapshots");
        f_modelMessage_messageSnapshots = com.discord.models.message.Message.class.getDeclaredField("messageSnapshots");

        // Overrides message content if the message is actually a forward
        patcher.patch(com.discord.models.message.Message.class.getDeclaredConstructor(Message.class), new Hook(callFrame -> {
            try {
                writeSnapshotFields(callFrame.args[0], callFrame.thisObject, true);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error(e);
            }
        }));

        // Keeps forward information when the message is updated (i.e. reacting)
        patcher.patch(com.discord.models.message.Message.class.getDeclaredMethod("copy", long.class, long.class, Long.class, User.class, String.class, UtcDateTime.class, UtcDateTime.class, Boolean.class, Boolean.class, List.class, List.class, List.class, List.class, List.class, String.class, Boolean.class, Long.class, Integer.class, MessageActivity.class, Application.class, Long.class, MessageReference.class, Long.class, List.class, List.class, Message.class, Interaction.class, Channel.class, List.class, MessageCall.class, Boolean.class, RoleSubscriptionData.class, boolean.class, MessageAllowedMentions.class, Integer.class, Long.class, Long.class, List.class, CaptchaHelper.CaptchaPayload.class), new Hook((callFrame) -> {
            try {
                callFrame.setResult(writeSnapshotFields(callFrame.thisObject, callFrame.getResult(), false));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error(e);
            }
        }));

        // Sets the bot tag to a FORWARDED tag, as it's the most convenient indication method
        patcher.patch(WidgetChatListAdapterItemMessage.class.getDeclaredMethod("configureItemTag", com.discord.models.message.Message.class, boolean.class), new PreHook((cf) -> {
            try {
                var snapshots = (ArrayList<MessageSnapshot>) f_modelMessage_messageSnapshots.get(cf.args[0]);
                if (snapshots == null || snapshots.isEmpty()) return;

                var tw = (TextView) ReflectUtils.getField(cf.thisObject, "itemTag");
                if (tw == null) return;

                tw.setVisibility(View.VISIBLE);
                tw.setText("FORWARDED");
                tw.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0); // Disables the verified checkmark, only done because RecyclerView

                cf.setResult(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error(e);
            }
        }));

        // Resolve our custom chat item so that it can actually be displayed
        patcher.patch(WidgetChatListAdapter.class.getDeclaredMethod("onCreateViewHolder", ViewGroup.class, int.class),
            new PreHook((cf) -> { // I did a PreHook to avoid looking up existing types, its not really necessary but idc :) - Wing (wingio)
                var _this = (WidgetChatListAdapter) cf.thisObject;
                int entryType = (int) cf.args[1];

                if (entryType == ForwardSourceChatEntry.FORWARD_SOURCE_ENTRY_TYPE) {
                    cf.setResult(new WidgetChatListAdapterItemForwardSource(_this));
                }
            }));

        // Add a custom ChatListEntry for forwarded message sources
        // Yes, the function signature is actually that long, Discord devs were insane - Wing (wingio)
        patcher.patch(WidgetChatListModelMessages.Companion.class.getDeclaredMethod("getMessageItems", Channel.class, Map.class, Map.class, Map.class, Channel.class, StoreThreadMessages.ThreadState.class, com.discord.models.message.Message.class, StoreMessageState.State.class, Map.class, boolean.class, boolean.class, Long.class, boolean.class, boolean.class, boolean.class, long.class, boolean.class, Map.class, InviteEmbedModel.class, boolean.class, boolean.class),
            new Hook((cf) -> {
                var items = (ArrayList<ChatListEntry>) cf.getResult(); // The return type for this method is List<T> but internally its an ArrayList<T>
                var msg = (com.discord.models.message.Message) cf.args[6];

                try {
                    var snapshots = (ArrayList<MessageSnapshot>) f_modelMessage_messageSnapshots.get(msg);
                    var reference = msg.getMessageReference();
                    if (reference == null) return;

                    var originalChannel = StoreStream.getChannels().getChannel(reference.a());
                    if (originalChannel == null) return; // This also implicitly checks if the user is in the source guild

                    // Checks if the current user has permission to access the source channel.
                    // This is really only done if the user is in the source guild but
                    // unable to access the source channel.
                    if (!PermissionUtils.INSTANCE.hasAccess(originalChannel, StoreStream.getPermissions().getPermissionsByChannel().get(reference.a()))) return;

                    if (snapshots != null && !snapshots.isEmpty()) { // Adds the source right before the list of reactions, if present
                        var reactionsEntry = items.stream().filter(chatListEntry -> chatListEntry instanceof ReactionsEntry).findFirst();
                        var reactionsIdx = items.indexOf(reactionsEntry.orElse(null));
                        items.add(reactionsIdx != -1 ? reactionsIdx : items.size(), new ForwardSourceChatEntry(reference, msg.getId()));
                    }
                } catch (Throwable e) {
                    logger.error(e);
                }

                cf.setResult(items);
            }));
    }

    @Override
    public void stop(Context context) throws Throwable {
        patcher.unpatchAll();
    }
}
