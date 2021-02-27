package com.discord.utilities.color;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;

import androidx.annotation.ColorInt;

@SuppressWarnings("unused")
public class ColorCompat {
    public static ColorCompat INSTANCE = new ColorCompat();

    public static int getThemedColor(Context context, int id) { return 0; }
    public static int getThemedColor(View view, int id) { return 0; }

    public final ColorStateList createDefaultColorStateList(@ColorInt int color) { return null; }
}
