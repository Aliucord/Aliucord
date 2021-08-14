package com.aliucord.views;

import android.content.Context;
import android.view.ContextThemeWrapper;

import com.google.android.material.button.MaterialButton;

import com.lytefast.flexinput.R;

/** Red MaterialButton */
public class DangerButton extends MaterialButton {
    /**
     * Creates a red Discord styled button.
     * @param context {@link Context}
     */
    public DangerButton(Context context) {
        super(new ContextThemeWrapper(context, R.h.UiKit_Material_Button_Red), null, 0);
    }
}
