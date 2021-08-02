/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lytefast.flexinput.R$c;
import com.lytefast.flexinput.R$d;

/** Discord Style Save Button */
public class SaveButton extends LinearLayout {
    public SaveButton(Context context) {
        super(context);
        FloatingActionButton saveButton = new FloatingActionButton(context);
        saveButton.setImageDrawable(ContextCompat.getDrawable(context, R$d.icon_save));
        saveButton.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R$c.brand, context.getTheme())));
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
        setHorizontalGravity(Gravity.RIGHT);
        setVerticalGravity(Gravity.BOTTOM);
        addView(saveButton);
    }
}