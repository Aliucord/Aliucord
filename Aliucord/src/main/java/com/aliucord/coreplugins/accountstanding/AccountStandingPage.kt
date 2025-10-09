/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.accountstanding

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.aliucord.*
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.ViewUtils.addTo
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R

data class ApiResponse(
    var account_standing: accountStandingState,
    var classifications: List<userClassifications>
) {
    data class userClassifications(val description: String)
    data class accountStandingState(val state: Int)
}

class AccountStandingPage : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        // Set both the action bar and subtitle
        setActionBarTitle("Checking account standing..")
        setActionBarSubtitle("User Settings")

        val context = view.context

        Utils.threadPool.execute {
            // Fetch the safety hub/account standing data
            try {
                val req = Http.Request.newDiscordRNRequest("/safety-hub/@me", "GET")
                val res = req.execute()
                val json = res.json(GsonUtils.gsonRestApi, ApiResponse::class.java)
                val number = json.account_standing.state
                var string: String?
                // Check if theres any violations or not
                var classificationsareEmpty = if (json.classifications.isEmpty()) true else false
                var user_classifications = if (!classificationsareEmpty) json.classifications.first() else null
                var violation = if (!classificationsareEmpty) user_classifications!!.description else null
                // Check to see whether the account is limited, very limited, at risk or banned (or just with no violations)
                when (number) {
                    100 -> string = "No current violations found."
                    200 -> string = "Your account seems limited."
                    300 -> string = "Your account is very limited."
                    400 -> string = "Your account is at risk of getting banned."
                    500 -> string = "Your account is banned."
                    else -> string = "Failed to check."
                }
                // Replaces the previous action bar title so its no longer checking
                Utils.mainThread.post {
                    setActionBarTitle("Account Standing")
                    createIndicator(context, number).addTo(linearLayout)
                    // Creates the TextView for the user to see their account standing/status
                    TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                        if (!classificationsareEmpty) text = string + " You've broken (or previously broken) the rules for $violation. (there may be more as well)" else text = string
                        typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                        gravity = Gravity.CENTER
                    }.addTo(linearLayout)
                }
            } catch (e: Exception) {
                // Log error and set the action bar title to failed (if theres an actual error)
                setActionBarTitle("Failed")
                Logger("AccountStanding").warn("Failed to check account standing", e)
            }
        }
    }

    // Creates the account standing indicator, adds circles and a line for it
    private fun createIndicator(context: Context, currentState: Int): LinearLayout {
        // Creates the container and circles for the indicator
        val container = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(DimenUtils.dpToPx(24), DimenUtils.dpToPx(24), DimenUtils.dpToPx(24), DimenUtils.dpToPx(24)) }
        val states = listOf(
            Triple(100, "All good", Utils.appContext.getColor(R.c.uikit_btn_bg_color_selector_green)),
            Triple(200, "Limited", Utils.appContext.getColor(R.c.status_yellow)),
            Triple(300, "Very limited", Utils.appContext.getColor(R.c.status_yellow)),
            Triple(400, "At risk", Utils.appContext.getColor(R.c.uikit_btn_bg_color_selector_red)),
            Triple(500, "Suspended", Utils.appContext.getColor(R.c.status_grey_200))
        )
        // Creates a bar that goes along the indicator
        val progressBar = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL}
        states.forEachIndexed { index, (state, _, color) ->
            FrameLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(DimenUtils.dpToPx(16), DimenUtils.dpToPx(16))
                    val circleDrawable = GradientDrawable().apply { shape = GradientDrawable.OVAL; if (currentState == state) setColor(color) else setColor(Utils.appContext.getColor(R.c.status_grey_200)) }
                    background = circleDrawable
            }.addTo(progressBar)

            // Add the circles
            if (index < states.size - 1) {
                // Sets the background color of the bar and adds it
                View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, DimenUtils.dpToPx(4), 1f)
                    setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorPrimaryDivider))
                }.addTo(progressBar)
            }
        }
        // Adds both the progressbar and labels
        container.addView(progressBar)
        val labelsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, DimenUtils.dpToPx(6), 0, 0) // Sets padding for each label
        }
        states.forEach { (_, label, _) ->
            TextView(context).apply {
                text = label // Adds the labels here, (e.g "All good", "Limited", "Suspended", etc)
                setTextColor(ColorCompat.getThemedColor(context, R.b.primary_300)) // Sets the text color (and also font size and font below)
                textSize = 12f
                typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }.addTo(labelsRow)
        }
        // Returns the view
        container.addView(labelsRow)
        return container
    }
}
