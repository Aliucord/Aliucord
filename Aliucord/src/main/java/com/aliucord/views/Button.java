package com.aliucord.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import com.google.android.material.button.MaterialButton;
import com.lytefast.flexinput.R$c;
import com.lytefast.flexinput.R$h;

@SuppressLint("ViewConstructor")
public class Button extends MaterialButton {
    public Button(Context context, boolean danger) {
        super(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) this.setTextAppearance(R$h.UiKit_TextAppearance_Button);
        setAllCaps(false);
        Resources res = context.getResources();
        this.setTextColor(res.getColor(R$c.uikit_btn_text_color_selector));
        if (danger) this.setBackgroundColor(res.getColor(R$c.uikit_btn_bg_color_selector_red));
        else this.setBackgroundColor(res.getColor(R$c.uikit_btn_bg_color_selector_brand));
    }
}
