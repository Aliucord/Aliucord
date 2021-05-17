package com.discord.utilities.spans;

import android.graphics.Paint;
import android.text.style.LineHeightSpan;

@SuppressWarnings("unused")
public final class VerticalPaddingSpan implements LineHeightSpan {
    public VerticalPaddingSpan(int top, int bottom) {}

    @Override
    public void chooseHeight(
            CharSequence text,
            int start,
            int end,
            int spanstartv,
            int lineHeight,
            Paint.FontMetricsInt fm
    ) {}
}
