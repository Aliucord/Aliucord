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
    public static void display(NotificationData data) {
        display(data, null);
    }

    public static void display(NotificationData data, Long channelId) {
        NoticePopup.enqueue$default(
                NoticePopup.INSTANCE,
                "InAppNotif#" + ClockFactory.get().currentTimeMillis(),
                data.getTitle(),
                data.getSubtitle(),
                data.getBody(),
                data.getAttachmentBackground(),
                data.getAttachmentUrl(),
                data.getAttachment(),
                data.getStickers(),
                data.getIconUrl(),
                data.getIconResId(),
                data.getIconTopRight(),
                data.getAutoDismissPeriodSecs(),
                data.getValidScreens(),
                data.getOnClickTopRightIcon(),
                (data.getOnClick() == null && channelId != null ? v -> {
                    ChannelSelector.getInstance().findAndSet(v.getContext(), channelId);
                    return Unit.a;
                } : data.getOnClick()),
                (data.getValidScreens() == null ? 4096 : 0) | (data.getOnClickTopRightIcon()== null ? 8192 : 0),
                null
        );
    }
}
