package com.discord.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;

@SuppressWarnings("unused")
public final class UsernameView extends ConstraintLayout {
    public UsernameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public final void setUsernameColor(@ColorInt int color) {}
    public final void setUsernameText(CharSequence text) {}
}
