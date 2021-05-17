package com.facebook.drawee.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public class SimpleDraweeView extends DraweeView {
    public SimpleDraweeView(Context context) {
        super(context);
    }

    public SimpleDraweeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleDraweeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SimpleDraweeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
