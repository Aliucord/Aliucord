package com.aliucord.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.aliucord.Utils;
import com.lytefast.flexinput.R;

/** Settings Header Toolbar Button */
public class ToolbarButton extends AppCompatImageButton {
    /**
     * Creates a header-toolbar button
     * @param context {@link Context}
     */
    public ToolbarButton(Context context) {
        super(new ContextThemeWrapper(context, R.h.UiKit_ImageView_Clickable), null, 0);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        setImageDrawable(drawable, true);
    }

    public void setImageDrawable(@Nullable Drawable drawable, boolean forceTint) {
        if (forceTint && drawable != null) {
            drawable = drawable.mutate();
            Utils.tintToTheme(getContext(), drawable);
        }
        super.setImageDrawable(drawable);
    }
}
