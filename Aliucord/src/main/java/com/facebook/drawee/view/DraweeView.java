package com.facebook.drawee.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class DraweeView extends ImageView {
    public DraweeView(Context context) {
        super(context);
    }

    public DraweeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DraweeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DraweeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
