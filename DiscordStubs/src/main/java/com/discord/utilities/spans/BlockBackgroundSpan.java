package com.discord.utilities.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public final class BlockBackgroundSpan implements LineBackgroundSpan {
    public BlockBackgroundSpan(@ColorInt int paintColor, @ColorInt int borderColor, int borderWidth, int radius, int leftMargin) {}

    public void drawBackground(
            @NonNull Canvas canvas,
            @NonNull Paint paint,
            int left,
            int right,
            int top,
            int baseline,
            int bottom,
            @NonNull CharSequence text,
            int start,
            int end,
            int lineNumber
    ) {}
}
