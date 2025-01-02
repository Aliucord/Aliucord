package com.aliucord.coreplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.aliucord.entities.CorePlugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.PreHook;
import com.aliucord.utils.ReflectUtils;
import com.discord.api.message.Message;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;

import java.util.ArrayList;

public class ForwardedMessages extends CorePlugin {
    public ForwardedMessages() {
        super(new Manifest("ForwardedMessages"));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void start(Context context) throws Throwable {

        patcher.patch(com.discord.models.message.Message.class.getDeclaredConstructor(Message.class), new Hook(callFrame -> {
            try {
                var snapshots = (ArrayList) ReflectUtils.getField(callFrame.args[0], "messageSnapshots");

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
                var snapshots = (ArrayList<Object>) ReflectUtils.getField(cf.args[0], "messageSnapshots");
                if (snapshots == null || snapshots.isEmpty()) return;
                var tw = (TextView) ReflectUtils.getField(cf.thisObject, "itemTag");
                tw.setVisibility(View.VISIBLE);
                tw.setText("FORWARDED");
                cf.setResult(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error(e);
            }
        }));
    }

    @Override
    public void stop(Context context) throws Throwable {
        patcher.unpatchAll();
    }
}
