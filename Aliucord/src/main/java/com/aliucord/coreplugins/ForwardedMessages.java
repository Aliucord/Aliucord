package com.aliucord.coreplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Utils;
import com.aliucord.entities.CorePlugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.PreHook;
import com.aliucord.updater.ManagerBuild;
import com.aliucord.utils.ReflectUtils;
import com.discord.api.message.Message;
import com.discord.api.permission.Permission;
import com.discord.stores.StoreStream;
import com.discord.utilities.PermissionOverwriteUtilsKt;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;

import java.lang.reflect.Field;
import java.util.ArrayList;

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

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("unchecked")
    @Override
    public void start(Context context) throws Throwable {
        if (!ManagerBuild.hasInjector("2.1.0") || !ManagerBuild.hasPatches("1.1.0")) {
            logger.warn("Base app outdated, cannot enable ForwardedMessages");
            return;
        }

        // Cache reflection since this is used in a performance-sensitive areas
        f_apiMessage_messageSnapshots = Message.class.getDeclaredField("messageSnapshots");
        f_modelMessage_messageSnapshots = com.discord.models.message.Message.class.getDeclaredField("messageSnapshots");

        patcher.patch(com.discord.models.message.Message.class.getDeclaredConstructor(Message.class), new Hook(callFrame -> {
            try {
                var snapshots = (ArrayList<Object>) f_apiMessage_messageSnapshots.get(callFrame.args[0]);

                if (snapshots == null || snapshots.isEmpty()) return;

                Message messageSnapshot = (Message) ReflectUtils.getField(snapshots.get(0), "message");

                ReflectUtils.setField(callFrame.thisObject, "messageSnapshots", snapshots);
                ReflectUtils.setField(callFrame.thisObject, "attachments", messageSnapshot.d());
                ReflectUtils.setField(callFrame.thisObject, "embeds", messageSnapshot.k());
                ReflectUtils.setField(callFrame.thisObject, "content", messageSnapshot.i());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error(e);
            }
        }));

        patcher.patch(WidgetChatListAdapterItemMessage.class.getDeclaredMethod("configureItemTag", com.discord.models.message.Message.class, boolean.class), new PreHook((cf) -> {
            try {
                var snapshots = (ArrayList<Object>) f_modelMessage_messageSnapshots.get(cf.args[0]);
                if (snapshots == null || snapshots.isEmpty()) return;

                var tw = (TextView) ReflectUtils.getField(cf.thisObject, "itemTag");
                if (tw == null) return;

                tw.setVisibility(View.VISIBLE);
                tw.setText("FORWARDED");

                cf.setResult(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error(e);
            }
        }));


        var viewId = View.generateViewId();
        Drawable icon = ContextCompat.getDrawable(context, com.lytefast.flexinput.R.e.ic_reply_24dp).mutate();
        icon.setTint(ColorCompat.getThemedColor(Utils.appActivity, com.lytefast.flexinput.R.b.colorInteractiveNormal));

        patcher.patch(WidgetChatListActions.class.getDeclaredMethod("configureUI", WidgetChatListActions.Model.class),
            new Hook((cf) -> {
                var actions = (WidgetChatListActions) cf.thisObject;
                var message = ((WidgetChatListActions.Model) cf.args[0]).getMessage();

                try {
                    ArrayList<Object> snapshots = (ArrayList<Object>) f_modelMessage_messageSnapshots.get(message);
                    if (snapshots == null || snapshots.isEmpty()) return;

                    var reference = message.getMessageReference();
                    var permissionOverwrites = StoreStream.getChannels().getChannel(reference.a()).v();

                    for (var permissionOverwrite : permissionOverwrites) {
                        if (PermissionOverwriteUtilsKt.denies(permissionOverwrite, Permission.VIEW_CHANNEL)) {
                            return;
                        }
                    }
                    var lay = (LinearLayout) ((NestedScrollView) actions.getView()).getChildAt(0);
                    if (lay.findViewById(viewId) == null) {
                        TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Icon);
                        tw.setId(viewId);
                        tw.setText("Jump to source");
                        tw.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                        lay.addView(tw, 8);
                        tw.setOnClickListener((v) -> {
                            StoreStream.getMessagesLoader().jumpToMessage(reference.a(), reference.c());
                            actions.dismiss();
                        });
                    }
                } catch (IllegalAccessException e) {
                    logger.error(e);
                }
            }));
    }

    @Override
    public void stop(Context context) throws Throwable {
        patcher.unpatchAll();
    }
}
