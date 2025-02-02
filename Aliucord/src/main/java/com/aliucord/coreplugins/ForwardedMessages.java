package com.aliucord.coreplugins;

import android.annotation.SuppressLint;
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
import com.discord.api.channel.Channel;
import com.discord.api.message.Message;
import com.discord.stores.*;
import com.discord.utilities.embed.InviteEmbedModel;
import com.discord.utilities.permissions.PermissionUtils;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.entries.ChatListEntry;
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

    @Override
    @SuppressLint("SetTextI18n")
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
                var snapshots = (ArrayList<Object>) f_apiMessage_messageSnapshots.get(callFrame.args[0]);

                if (snapshots == null || snapshots.isEmpty()) return;

                Message messageSnapshot = (Message) ReflectUtils.getField(snapshots.get(0), "message");
                assert messageSnapshot != null; // We can assume that if we're given a snapshot that its message field is present

                ReflectUtils.setField(callFrame.thisObject, "messageSnapshots", snapshots);
                ReflectUtils.setField(callFrame.thisObject, "embeds", messageSnapshot.k());
                ReflectUtils.setField(callFrame.thisObject, "content", messageSnapshot.i());
                ReflectUtils.setField(callFrame.thisObject, "attachments", messageSnapshot.d());
                ReflectUtils.setField(callFrame.thisObject, "stickerItems", messageSnapshot.A());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error(e);
            }
        }));

        // Sets the bot tag to a FORWARDED tag, as it's the most convenient indication method
        patcher.patch(WidgetChatListAdapterItemMessage.class.getDeclaredMethod("configureItemTag", com.discord.models.message.Message.class, boolean.class), new PreHook((cf) -> {
            try {
                var snapshots = (ArrayList<Object>) f_modelMessage_messageSnapshots.get(cf.args[0]);
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
                    var snapshots = (ArrayList<Object>) f_modelMessage_messageSnapshots.get(msg);
                    var reference = msg.getMessageReference();
                    if (reference == null) return;

                    var originalChannel = StoreStream.getChannels().getChannel(reference.a());
                    if (originalChannel == null) return; // This also implicitly checks if the user is in the source guild

                    // Checks if the current user has permission to access the source channel.
                    // This is really only done if the user is in the source guild but
                    // unable to access the source channel.
                    if (!PermissionUtils.INSTANCE.hasAccess(originalChannel, StoreStream.getPermissions().getPermissionsByChannel().get(reference.a()))) return;

                    if (snapshots != null && !snapshots.isEmpty()) {
                        items.add(new ForwardSourceChatEntry(reference, msg.getId()));
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
