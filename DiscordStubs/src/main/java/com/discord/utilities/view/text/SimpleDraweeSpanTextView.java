package com.discord.utilities.view.text;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.facebook.drawee.span.DraweeSpanStringBuilder;

@SuppressWarnings("unused")
public class SimpleDraweeSpanTextView extends AppCompatTextView {
    private DraweeSpanStringBuilder mDraweeStringBuilder;

    public SimpleDraweeSpanTextView(@NonNull Context context) {
        super(context);
    }

    public void detachCurrentDraweeSpanStringBuilder() {}

    public void onFinishTemporaryDetach() {}

    public void onStartTemporaryDetach() {}

    public void setDraweeSpanStringBuilder(DraweeSpanStringBuilder builder) {}
}
