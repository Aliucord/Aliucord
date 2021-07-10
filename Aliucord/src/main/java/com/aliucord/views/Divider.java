/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.aliucord.Utils;
import com.discord.utilities.color.ColorCompat;
import com.lytefast.flexinput.R$b;

public class Divider extends View {
    public Divider(Context context) {
        super(context);
        this.setBackgroundColor(ColorCompat.getThemedColor(context, R$b.colorPrimaryDivider));
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, Utils.dpToPx(0.25f));
        this.setLayoutParams(params);
    }
}
