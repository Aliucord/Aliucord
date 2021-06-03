/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.discord.app.AppComponent;
import com.discord.models.sticker.dto.ModelSticker;

import java.util.List;

import c0.e0.c;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class NotificationData {
    public CharSequence title;
    public CharSequence subtitle;
    public CharSequence body;
    public Drawable attachmentBackground;
    public String attachmentUrl;
    public Drawable attachment;
    public List<ModelSticker> stickers;
    public String iconUrl = "";
    public Integer iconResId;
    public Drawable iconTopRight;
    public Integer autoDismissPeriodSecs = 5;
    public List<? extends c<? extends AppComponent>> validScreens;
    public Function1<? super View, Unit> onClickTopRightIcon;
    public Function1<? super View, Unit> onClick;
}
