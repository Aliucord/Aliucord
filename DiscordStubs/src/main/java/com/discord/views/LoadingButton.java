package com.discord.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public final class LoadingButton extends FrameLayout {
    public LoadingButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public final void setIsLoading(boolean v) {}
    public final void setText(CharSequence text) {}
}
