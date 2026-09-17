package com.aliucord.coreplugins.voice.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R

internal fun rowTitle(ctx: Context, text: String): TextView =
    TextView(ctx, null, 0, R.i.UserProfile_Section_Header).apply {
        this.text = text
        this.setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorHeaderPrimary))
        this.typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_semibold)
        this.textSize = 16f
        this.isAllCaps = false
    }

internal fun rowContainer(root: LinearLayout): LinearLayout =
    (root.getChildAt(0) as? LinearLayout) ?: root
