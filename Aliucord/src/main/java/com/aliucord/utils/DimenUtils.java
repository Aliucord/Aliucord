/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import com.aliucord.Utils;

public final class DimenUtils {
    private static final float density = Utils.getAppContext().getResources().getDisplayMetrics().density;

    /**
     * Converts DP to PX.
     * @param dp DP value
     * @return <code>DP</code> converted to PX
     */
    public static int dpToPx(int dp) { return dpToPx((float) dp); }

    /**
     * Converts DP to PX.
     * @param dp DP value
     * @return <code>DP</code> converted to PX
     */
    public static int dpToPx(float dp) { return (int) (dp * density + 0.5f); }

    /**
     * Converts PX to DP.
     * @param px PX value
     * @return <code>PX</code> converted to DP
     * @see com.discord.utilities.dimen.DimenUtils#pixelsToDp(int) 
     */
    public static int pxToDp(int px) { return com.discord.utilities.dimen.DimenUtils.pixelsToDp(px); }

    private static int defaultPadding = 0;
    private static int defaultCardRadius = 0;

    /**
     * Gets the default padding for the items. (16 DP)
     * @return default padding
     * @see DimenUtils#dpToPx(int)
     * @see DimenUtils#dpToPx(float)
     */
    public static int getDefaultPadding() {
        if (defaultPadding == 0) defaultPadding = dpToPx(16);
        return defaultPadding;
    }

    /**
     * Gets the default radius for cards. (8 DP)
     * @return default padding
     * @see DimenUtils#dpToPx(int)
     * @see DimenUtils#dpToPx(float)
     */
    public static int getDefaultCardRadius() {
        if (defaultCardRadius == 0) defaultCardRadius = dpToPx(8);
        return defaultCardRadius;
    }
}
