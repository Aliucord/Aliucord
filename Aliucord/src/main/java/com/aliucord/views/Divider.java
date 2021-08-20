/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.aliucord.utils.DimenUtils;
import com.discord.utilities.color.ColorCompat;
import com.lytefast.flexinput.R;

/** Discord style Divider as seen in its settings */
public class Divider extends View {
    public Divider(Context context) {
        super(context);
        this.setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorPrimaryDivider));
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, DimenUtils.dpToPx(0.25f));
        this.setLayoutParams(params);
    }
}
