/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.utils

import com.aliucord.Utils.appContext

@Suppress("NOTHING_TO_INLINE")
object DimenUtils {
    @PublishedApi
    internal val density = appContext.resources.displayMetrics.density

    /**
     * The default padding for items (16 DP)
     */
    @JvmStatic
    val defaultPadding = dpToPx(16)

    /**
     * The default padding for cards (8 DP)
     */
    @JvmStatic
    val defaultCardRadius = dpToPx(8)

    /**
     * Converts DP to PX
     * @return `DP` converted to PX
     */
    inline val Int.dp: Int
        get() = dpToPx(this)

    /**
     * Converts DP to PX
     * @return `DP` converted to PX
     */
    inline val Float.dp: Int
        get() = dpToPx(this)

    /**
     * Converts DP to PX.
     * @param dp DP value
     * @return `DP` converted to PX
     */
    @JvmStatic
    inline fun dpToPx(dp: Int): Int = dpToPx(dp.toFloat())

    /**
     * Converts DP to PX.
     * @param dp DP value
     * @return `DP` converted to PX
     */
    @JvmStatic
    inline fun dpToPx(dp: Float): Int = (dp * density + 0.5f).toInt()

    /**
     * Converts PX to DP.
     * @param px PX value
     * @return `PX` converted to DP
     */
    @JvmStatic
    inline fun pxToDp(px: Int): Int = (px / density).toInt()
}
