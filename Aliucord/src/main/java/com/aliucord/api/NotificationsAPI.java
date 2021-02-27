package com.aliucord.api;

import com.aliucord.Utils;
import com.discord.utilities.fcm.NotificationData;
import com.discord.utilities.fcm.NotificationRenderer;

import java.util.HashMap;
import java.util.Map;

// NOTE: Very basic NotificationsAPI, there should be more options soon.

public class NotificationsAPI {
    public static void display(String title, String content, Long channelId) {
        Map<String, String> map = new HashMap<>();
        map.put("type", "ALIUCORD");
        map.put("guild_name", title);
        map.put("message_content", content);
        map.put("channel_id", String.valueOf(channelId));
        NotificationRenderer.INSTANCE.displayInApp(Utils.getAppContext(), new NotificationData(map));
    }
}
