/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.accountstanding

import android.annotation.SuppressLint
import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils.defaultCardRadius
import com.aliucord.utils.DimenUtils.defaultPadding
import com.aliucord.utils.ViewUtils.setDefaultMargins
import com.discord.api.utcdatetime.UtcDateTime
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.time.ClockFactory
import com.discord.utilities.time.TimeUtils
import com.google.android.material.card.MaterialCardView
import com.lytefast.flexinput.R
import java.util.Date

@SuppressLint("ViewConstructor")
internal class ViolationCard(ctx: Context, violation: String, flaggedContent: String?, actions: List<String>, id: Long, maxExpirationTime: UtcDateTime) : MaterialCardView(ctx) {
    var title: TextView

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        setDefaultMargins(false, true, false, false)

        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        radius = defaultCardRadius.toFloat()
        setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
        setCardBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundSecondaryAlt))
        if (Date().time > maxExpirationTime.g()) alpha = 0.5f

        title = TextView(ctx, null, 0, R.i.UiKit_Settings_Item).apply {
            text = "${TimeUtils.toReadableTimeString(context, SnowflakeUtils.toTimestamp(id), ClockFactory.get())}\nYou broke Discord's rules for $violation"
            textSize = 14f
            typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_semibold)
            movementMethod = LinkMovementMethod.getInstance()
            setOnClickListener {
                Utils.openPageWithProxy(ctx, ViolationPage(violation, flaggedContent, actions, id, maxExpirationTime))
            }
        }

        addView(title)
    }
}
