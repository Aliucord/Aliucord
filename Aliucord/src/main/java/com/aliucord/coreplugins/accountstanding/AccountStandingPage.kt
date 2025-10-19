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

@Suppress("MISSING_DEPENDENCY_SUPERCLASS")
class AccountStandingPage : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Account Standing")
        setActionBarSubtitle("User Settings")

        val context = view.context

        Utils.threadPool.execute {
            try {
                val json = Http.Request.newDiscordRNRequest("/safety-hub/@me", "GET").execute()
                    .json(GsonUtils.gsonRestApi, SafetyHubResponse::class.java)

                val userClassifications = if (json.classifications.isNotEmpty()) json.classifications.first() else null
                val violation = if (json.classifications.isNotEmpty()) userClassifications!!.description else null

                Utils.mainThread.post {
                    createIndicator(context, json.accountStanding.state).addTo(linearLayout)

                    TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                        text = if (json.classifications.isNotEmpty()) "${determineString(json.accountStanding.state)} You broke (or previously broke) Discord's rules for $violation. (there may be more as well)" else determineString(json.accountStanding.state)
                        typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                        gravity = Gravity.CENTER
                    }.addTo(linearLayout)
                }
            } catch (e: Exception) {
                Logger("AccountStanding").errorToast("Failed to check account standing", e)
            }
        }
    }

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

    private fun createIndicator(context: Context, currentState: Int): LinearLayout {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 24.dp, 24.dp, 24.dp)
        }

        val states = arrayOf(
            ClassificationState(100, "All good", Utils.appContext.getColor(R.c.uikit_btn_bg_color_selector_green)),
            ClassificationState(200, "Limited", Utils.appContext.getColor(R.c.status_yellow)),
            ClassificationState(300, "Very limited", Utils.appContext.getColor(R.c.status_yellow)),
            ClassificationState(400, "At risk", Utils.appContext.getColor(R.c.uikit_btn_bg_color_selector_red)),
            ClassificationState(500, "Suspended", Utils.appContext.getColor(R.c.status_grey_200))
        )

        val progressBar = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
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

            if (index < states.size - 1) {
                View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 4.dp, 1f)
                    setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorPrimaryDivider))
                }.addTo(progressBar)
            }
        }

        container.addView(progressBar)
        val labelsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 6.dp, 0, 0)
        }

        states.forEach { (_, label, _) ->
            TextView(context).apply {
                text = label
                setTextColor(ColorCompat.getThemedColor(context, R.b.primary_300))
                textSize = 12f
                typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }.addTo(labelsRow)
        }

        container.addView(labelsRow)
        return container
    }
}
