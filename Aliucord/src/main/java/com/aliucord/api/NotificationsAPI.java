/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api;

import com.aliucord.entities.NotificationData;
import com.discord.utilities.channel.ChannelSelector;
import com.discord.utilities.time.ClockFactory;
import com.discord.widgets.notice.NoticePopup;

import kotlin.Unit;

public class NotificationsAPI {

    /**
     * Displays a notification to the user.
     * @param data {@link NotificationData}
     * @see NotificationsAPI#display(NotificationData, Long)
     */
    public static void display(NotificationData data) {
        display(data, null);
    }

    /**
     * Displays a notification to the user in a specific channel.
     * @param data {@link NotificationData}
     * @param channelId Channel to display the notification in.
     */
    public static void display(NotificationData data, Long channelId) {
        NoticePopup.enqueue$default(
                NoticePopup.INSTANCE,
                "InAppNotif#" + ClockFactory.get().currentTimeMillis(),
                data.title,
                data.subtitle,
                data.body,
                data.attachmentBackground,
                data.attachmentUrl,
                data.attachment,
                data.stickers,
                data.iconUrl,
                data.iconResId,
                data.iconTopRight,
                data.autoDismissPeriodSecs,
                data.validScreens,
                data.onClickTopRightIcon,
                (data.onClick == null && channelId != null ? v -> {
                    ChannelSelector.getInstance().findAndSet(v.getContext(), channelId);
                    return Unit.a;
                } : data.onClick),
                (data.validScreens == null ? 4096 : 0) | (data.onClickTopRightIcon == null ? 8192 : 0),
                null
        );
    }
}
