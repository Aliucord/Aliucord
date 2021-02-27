package com.aliucord.coreplugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.api.PatcherAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PrePatchRes;
import com.discord.utilities.fcm.NotificationData;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationHandler extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() { return new Manifest(); }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("com.discord.utilities.fcm.NotificationData", Arrays.asList("getContent", "getTitle", "getIconUrl"));
        map.put("com.discord.utilities.fcm.NotificationRenderer", Collections.singletonList("displayInApp"));
        return map;
    }

    public Runnable _unpatch = null;

    @Override
    public void start(Context context) {
        final Logger logger = new Logger("NotificationHandler");

        Field _contentField = null;
        try {
            _contentField = NotificationData.class.getDeclaredField("messageContent");
            _contentField.setAccessible(true);
        } catch (Exception e) { logger.error(e); }
        final Field contentField = _contentField;

        Field _guildNameField = null;
        try {
            _guildNameField = NotificationData.class.getDeclaredField("guildName");
            _guildNameField.setAccessible(true);
        } catch (Exception e) { logger.error(e); }
        final Field guildNameField = _guildNameField;
//        NotificationData.class.getDeclaredField("messageContent");

        String className = "com.discord.utilities.fcm.NotificationData";
        Patcher.addPrePatch(className, "getContent", (_this, args) -> {
            NotificationData data = (NotificationData) _this;
            if (isACNotif(data) && contentField != null) {
                try {
                    CharSequence content = Utils.renderMD((String) contentField.get(data));
                    return new PrePatchRes(args, content);
                } catch (Exception e) { logger.error(e); }
            }
            return new PrePatchRes(args);
        });

        Patcher.addPrePatch(className, "getTitle", (_this, args) -> {
            NotificationData data = (NotificationData) _this;
            if (isACNotif(data) && guildNameField != null) {
                try {
                    CharSequence title = Utils.renderMD((String) guildNameField.get(data));
                    return new PrePatchRes(args, title);
                } catch (Exception e) { logger.error(e); }
            }
            return new PrePatchRes(args);
        });

        String rendererClassName = "com.discord.utilities.fcm.NotificationRenderer";
        Patcher.addPrePatch(rendererClassName, "displayInApp", (_this, args) -> {
            NotificationData data = (NotificationData) args.get(1);
            if (isACNotif(data)) {
                _unpatch = PatcherAPI.addPrePatch(className, "getChannelId", (_this1, args1) -> new PrePatchRes(args1, -1));
            }
            return new PrePatchRes(args);
        });
        Patcher.addPatch(rendererClassName, "displayInApp", (_this, args, ret) -> {
            NotificationData data = (NotificationData) args.get(1);
            if (isACNotif(data) && _unpatch != null) {
                _unpatch.run();
                _unpatch = null;
            }
            return ret;
        });
    }

    @Override
    public void stop(Context context) {}

    private boolean isACNotif(NotificationData data) {
        String type = data.getType();
        return type != null && type.equals("ALIUCORD");
    }
}
