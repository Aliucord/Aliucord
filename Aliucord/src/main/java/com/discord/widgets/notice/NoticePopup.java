package com.discord.widgets.notice;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.discord.app.AppComponent;
import com.discord.models.sticker.dto.ModelSticker;

import java.util.List;

import c0.d0.c;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@SuppressWarnings({"unused", "InstantiationOfUtilityClass"})
public final class NoticePopup {
    public static final NoticePopup INSTANCE = new NoticePopup();

    public static void enqueue$default(
            NoticePopup instance,
            String noticeName,
            CharSequence title,
            CharSequence subtitle,
            CharSequence body,
            Drawable attachmentBackground,
            String attachmentUrl,
            Drawable attachment,
            List<ModelSticker> stickers,
            String iconUrl,
            Integer iconResId,
            Drawable iconTopRight,
            Integer autoDismissPeriodSecs,
            List<? extends c<? extends AppComponent>> validScreens,
            Function1<? super View, Unit> onClickTopRightIcon,
            Function1<? super View, Unit> onClick,
            int flags,
            Object obj
    ) {}
}
