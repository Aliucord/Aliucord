/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.views

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams
import com.aliucord.utils.DimenUtils
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R

/** Discord style Divider as seen in its settings  */
class Divider(context: Context) : View(context) {
    init {
        setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorPrimaryDivider))
        val params = LayoutParams(LayoutParams.MATCH_PARENT, DimenUtils.dpToPx(0.25f))
        layoutParams = params
    }
}
