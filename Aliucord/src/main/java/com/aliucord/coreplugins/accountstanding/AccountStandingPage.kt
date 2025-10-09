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
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.SerializedName

data class SafetyHubResponse(
    @SerializedName("account_standing")
    var accountStanding: AccountStandingState,
    var classifications: List<UserClassifications>
) {
    data class UserClassifications(val description: String)
    data class AccountStandingState(val state: Int)
}

private data class ClassificationState(val state: Int, val string: String, val color: Int)

class AccountStandingPage : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        // Set both the action bar and subtitle
        setActionBarTitle("Account Standing")
        setActionBarSubtitle("User Settings")

        val context = view.context

        Utils.threadPool.execute {
            try {
                // Fetch the safety hub/account standing data
                val json = Http.Request.newDiscordRNRequest("/safety-hub/@me", "GET").execute()
                    .json(GsonUtils.gsonRestApi, SafetyHubResponse::class.java)
                var string = determineString(json.accountStanding.state)

                // Check if theres any violations or not (and also check if classifications are empty)
                var classificationsareEmpty = if (json.classifications.isEmpty()) true else false; var user_classifications = if (!classificationsareEmpty) json.classifications.first() else null
                var violation = if (!classificationsareEmpty) user_classifications!!.description else null

                // Replaces the previous action bar title so its no longer checking
                Utils.mainThread.post {
                    setActionBarTitle("Account Standing")
                    createIndicator(context, json.accountStanding.state).addTo(linearLayout)

                    // Creates the TextView for the user to see their account standing/status
                    TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                        text = if (!classificationsareEmpty) string + " You've broke (or previously broken) Discord's rules for $violation. (there may be more as well)" else string
                        typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                        gravity = Gravity.CENTER
                    }.addTo(linearLayout)
                }
            } catch (e: Exception) {
                // Log error and set the action bar title to failed (if theres an actual error)
                Logger("AccountStanding").errorToast("Failed to check account standing", e)
            }
        }
    }

    // Check to see whether the account is limited, very limited, at risk or banned (or just with no violations)
    private fun determineString(state: Int): String {
        return when (state) {
            100 -> "No current violations found."
            200 -> "Your account seems limited."
            300 -> "Your account is very limited."
            400 -> "Your account is at risk of getting banned."
            500 -> "Your account is banned."
            else -> "Unknown"
        }
    }

    // Creates the account standing indicator, adds circles and a line for it
    private fun createIndicator(context: Context, currentState: Int): LinearLayout {
        // Creates the container and circles for the indicator
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL // Sets both the orientation and padding to 24xp
            setPadding(24.dp, 24.dp, 24.dp, 24.dp)
        }

        // Add the array of states, strings and colors to be used
        val states = arrayOf(
            ClassificationState(100, "All good", Utils.appContext.getColor(R.c.uikit_btn_bg_color_selector_green)),
            ClassificationState(200, "Limited", Utils.appContext.getColor(R.c.status_yellow)),
            ClassificationState(300, "Very limited", Utils.appContext.getColor(R.c.status_yellow)),
            ClassificationState(400, "At risk", Utils.appContext.getColor(R.c.uikit_btn_bg_color_selector_red)),
            ClassificationState(500, "Suspended", Utils.appContext.getColor(R.c.status_grey_200))
        )

        // Creates a bar that goes along the indicator
        val progressBar = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL;
            gravity = Gravity.CENTER_VERTICAL
        }

        states.forEachIndexed { index, (state, _, color) ->
            FrameLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(16.dp, 16.dp)
                    val circleDrawable = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        if (currentState == state) setColor(color) else setColor(Utils.appContext.getColor(R.c.status_grey_200))
                    }
                    background = circleDrawable
            }.addTo(progressBar)

            // Add the circles
            if (index < states.size - 1) {
                // Sets the background color of the bar and adds it
                View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 4.dp, 1f)
                    setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorPrimaryDivider))
                }.addTo(progressBar)
            }
        }

        // Adds both the progressbar and labels
        container.addView(progressBar)
        val labelsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 6.dp, 0, 0) // Sets padding for each label
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
