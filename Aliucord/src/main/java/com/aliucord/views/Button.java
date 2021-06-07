/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.views;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.google.android.material.button.MaterialButton;
import com.lytefast.flexinput.R$c;
import com.lytefast.flexinput.R$h;

public class Button extends MaterialButton {

    /**
     * @deprecated Use {@link Button#Button(Context)}
     */
    @Deprecated
    public Button(Context context, boolean danger) {
        super(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) this.setTextAppearance(R$h.UiKit_TextAppearance_Button);
        setAllCaps(false);
        Resources res = context.getResources();
        setTextColor(res.getColor(R$c.uikit_btn_text_color_selector));
        if (danger) setBackgroundColor(res.getColor(R$c.uikit_btn_bg_color_selector_red));
        else setBackgroundColor(res.getColor(R$c.brand));
    }

    public Button(Context context) {
        super(new ContextThemeWrapper(context, R$h.UiKit_Material_Button), null, 0);
    }

}
