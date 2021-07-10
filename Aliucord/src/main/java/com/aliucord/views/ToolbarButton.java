package com.aliucord.views;

import android.content.Context;
import android.view.ContextThemeWrapper;

import androidx.appcompat.widget.AppCompatImageButton;

import com.lytefast.flexinput.R$h;

/** Settings Header Toolbar Button */
public class ToolbarButton extends AppCompatImageButton {
    /**
     * Creates a header-toolbar button
     * @param context {@link Context}
     */
    public ToolbarButton(Context context) {
        super(new ContextThemeWrapper(context, R$h.UiKit_ImageView_Clickable), null, 0);
    }

}
