/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.accountstanding

import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.aliucord.*
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.MDUtils
import com.aliucord.utils.ViewUtils.addTo
import com.lytefast.flexinput.R
import com.aliucord.utils.ViewUtils.findViewById
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.time.ClockFactory
import com.discord.utilities.time.TimeUtils
import com.discord.utilities.SnowflakeUtils
import com.discord.api.utcdatetime.UtcDateTime

internal class ViolationPage(
    private val violation: String,
    private val flaggedContent: String,
    private val actions: List<String>,
    private var classificationId: Long,
    private val maxExpirationTime: UtcDateTime
) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        val header = view.findViewById<ViewGroup>("action_bar_toolbar")

        // Remove the header title and description (since it's unneeded) and change the background color to the primary background color.
        header.removeViewAt(0)
        header.setBackgroundColor(ColorCompat.getThemedColor(view.context, R.b.colorBackgroundMobilePrimary))

        TextView(context, null, 0, R.i.UiKit_Settings_Item).apply {
            text = MDUtils.render("You broke Discord's rules for **$violation**")
            textSize = 21f
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
        }.addTo(linearLayout)

        TextView(context, null, 0, R.i.UiKit_Settings_Item).apply {
            text = "Flagged content: $flaggedContent"
            textSize = 16f
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
            setPadding(16.dp, 0.dp, 0.dp, 0.dp)
        }.addTo(linearLayout)

        TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
            text = "What this means for you:\n${actions.joinToString("\n")}"
            textSize = 16f
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
        }.addTo(linearLayout)

        TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
            val occurredTime = TimeUtils.toReadableTimeString(context, SnowflakeUtils.toTimestamp(classificationId), ClockFactory.get())
            val expirationTime = TimeUtils.toReadableTimeString(context,
                SnowflakeUtils.toTimestamp(TimeUtils.millisToSnowflake(maxExpirationTime.g())),
                ClockFactory.get())

            text = "Violation will expire on $expirationTime\nOccurred on $occurredTime"
            textSize = 14f
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
        }.addTo(linearLayout)
    }
}
