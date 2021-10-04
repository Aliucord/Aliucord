/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.views;

import android.content.Context;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;

import com.google.android.material.button.MaterialButton;
import com.lytefast.flexinput.R;

/** Brand-themed MaterialButton */
public class Button extends MaterialButton {
    /**
     * Creates a Discord styled button.
     * @param context {@link Context}
     */
    public Button(Context context) {
        super(new ContextThemeWrapper(context, R.h.UiKit_Material_Button), null, 0);
    }
}
