/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.views;

import android.content.Context;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;

import com.google.android.material.button.MaterialButton;
import com.lytefast.flexinput.R$c;
import com.lytefast.flexinput.R$h;

/** Brand-themed MaterialButton */
public class Button extends MaterialButton {

    /**
     * @deprecated Use {@link Button#Button(Context)}
     */
    @Deprecated
    public Button(Context context, boolean danger) {
        super(context);
        setTextAppearance(R$h.UiKit_TextAppearance_Button);
        setAllCaps(false);
        Resources res = context.getResources();
        setTextColor(res.getColor(R$c.uikit_btn_text_color_selector, null));
        if (danger) setBackgroundColor(res.getColor(R$c.uikit_btn_bg_color_selector_red, null));
        else setBackgroundColor(res.getColor(R$c.brand, null));
    }

    /**
     * Creates a Discord styled button.
     * @param context {@link Context}
     */
    public Button(Context context) {
        super(new ContextThemeWrapper(context, R$h.UiKit_Material_Button), null, 0);
    }

}
