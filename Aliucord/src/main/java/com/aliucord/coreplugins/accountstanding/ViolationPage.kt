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

class ViolationPage(var violation: String, var flaggedContent: String?, var actions: List<String>) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        val header = view.findViewById<ViewGroup>("action_bar_toolbar")

        // Change the UI a little bit
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
            setPadding(16.dp, 1.dp, 1.dp, 1.dp)
        }.addTo(linearLayout)

        var actionsText: String? = ""
        for (i in actions) {
            actionsText += "- $i\n"
        }

        TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
            text = "What this means for you:\n$actionsText"
            textSize = 16f
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
        }.addTo(linearLayout)
    }
}
