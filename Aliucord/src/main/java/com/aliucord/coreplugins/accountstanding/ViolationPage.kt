/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.accountstanding

import android.text.SpannableStringBuilder
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

internal class ViolationPage(val violation: String, val flaggedContent: String?, val actions: List<String>, var id: Long, val maxExpirationTime: UtcDateTime) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        val header = view.findViewById<ViewGroup>("action_bar_toolbar")
        val occurredTime = TimeUtils.toReadableTimeString(context, SnowflakeUtils.toTimestamp(id), ClockFactory.get())
        val expirationTime = TimeUtils.toReadableTimeString(context, SnowflakeUtils.toTimestamp(TimeUtils.millisToSnowflake(maxExpirationTime.g())), ClockFactory.get())

        // Remove the header title and description (since it's unneeded) and change the background color to the regular current color.
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

        val actionsText = SpannableStringBuilder().apply {
            for (i in actions) {
                "- $i\n"
            }
        }

        TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
            text = "What this means for you:\n$actionsText"
            textSize = 16f
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
        }.addTo(linearLayout)

        TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
            text = "Violation will expire on $expirationTime\nOccurred on $occurredTime"
            textSize = 14f
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
        }.addTo(linearLayout)
    }
}
