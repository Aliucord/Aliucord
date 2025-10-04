/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Http
import com.aliucord.Http.*
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.*
import com.aliucord.utils.DimenUtils
import com.lytefast.flexinput.R
import java.util.*

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
        setActionBarTitle("Checking account standing..")
        setActionBarSubtitle("User Settings")

        val context = view.context

        Utils.threadPool.execute {
            try {
                val req = Http.Request.newDiscordRNRequest("/safety-hub/@me", "GET")
                val res = req.execute()
                val json = res.json(GsonUtils.gsonRestApi, ApiResponse::class.java)
                val number = json.account_standing.state

                var classificationsareEmpty = if (json.classifications.isEmpty()) true else false
                var user_classifications =
                        if (!classificationsareEmpty) json.classifications.first() else null
                var violation =
                        if (!classificationsareEmpty) user_classifications!!.description else null

                Utils.mainThread.post {
                    setActionBarTitle("Account Standing")

                    // Create the progress indicator
                    val progressContainer = createAccountStandingIndicator(context, number)
                    linearLayout.addView(progressContainer)

                    // Add description text below
                    TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).run {
                        val statusText =
                                when (number) {
                                    100 -> "No violations found."
                                    200 -> "Your account seems limited,"
                                    300 -> "Your account is very limited,"
                                    400 -> "Your account is at risk of getting banned,"
                                    500 -> "Your account is banned,"
                                    else -> "Failed to check."
                                }
                        text =
                                if (!classificationsareEmpty) {
                                    "$statusText You've broke the rules for $violation (there might be some more as well)."
                                } else {
                                    statusText
                                }
                        typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                        gravity = Gravity.CENTER
                        setPadding(
                                DimenUtils.dpToPx(16),
                                DimenUtils.dpToPx(16),
                                DimenUtils.dpToPx(16),
                                DimenUtils.dpToPx(16)
                        )
                        linearLayout.addView(this)
                    }
                }
            } catch (e: Exception) {
                setActionBarTitle("Failed")
                Logger("AccountStanding").warn("Failed to check account standing", e)
            }
        }
    }

    private fun createAccountStandingIndicator(
            context: android.content.Context,
            currentState: Int
    ): LinearLayout {
        val container =
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(
                            DimenUtils.dpToPx(24),
                            DimenUtils.dpToPx(24),
                            DimenUtils.dpToPx(24),
                            DimenUtils.dpToPx(16)
                    )
                }

        // State definitions
        val states =
                listOf(
                        Triple(100, "All good!", Color.parseColor("#3BA55C")),
                        Triple(200, "Limited", Color.parseColor("#FAA61A")),
                        Triple(300, "Very limited", Color.parseColor("#FAA61A")),
                        Triple(400, "At risk", Color.parseColor("#ED4245")),
                        Triple(500, "Suspended", Color.parseColor("#747F8D"))
                )

        // Create progress bar with circles
        val progressBar =
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

        states.forEachIndexed { index, (state, _, color) ->
            // Add circle
            val circle =
                    FrameLayout(context).apply {
                        layoutParams =
                                LinearLayout.LayoutParams(
                                        DimenUtils.dpToPx(32),
                                        DimenUtils.dpToPx(32)
                                )

                        val circleDrawable =
                                GradientDrawable().apply {
                                    shape = GradientDrawable.OVAL
                                    if (currentState >= state) {
                                        setColor(color)
                                    } else {
                                        setColor(Color.parseColor("#4F545C"))
                                    }
                                }
                        background = circleDrawable

                        // Add checkmark for completed states
                        if (currentState >= state) {
                            addView(
                                    TextView(context).apply {
                                        text = "✓"
                                        setTextColor(Color.WHITE)
                                        textSize = 16f
                                        gravity = Gravity.CENTER
                                        layoutParams =
                                                FrameLayout.LayoutParams(
                                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                                        FrameLayout.LayoutParams.MATCH_PARENT
                                                )
                                    }
                            )
                        }
                    }
            progressBar.addView(circle)

            // Add connecting line (except after last circle)
            if (index < states.size - 1) {
                val line =
                        View(context).apply {
                            layoutParams = LinearLayout.LayoutParams(0, DimenUtils.dpToPx(4), 1f)
                            setBackgroundColor(
                                    if (currentState > state) color else Color.parseColor("#4F545C")
                            )
                        }
                progressBar.addView(line)
            }
        }

        container.addView(progressBar)

        // Create labels row
        val labelsRow =
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, DimenUtils.dpToPx(8), 0, 0)
                }

        states.forEach { (state, label, _) ->
            val labelView =
                    TextView(context).apply {
                        text = label
                        textSize = 12f
                        setTextColor(
                                if (currentState == state) Color.WHITE
                                else Color.parseColor("#B9BBBE")
                        )
                        gravity = Gravity.CENTER
                        layoutParams =
                                LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f
                                )
                    }
            labelsRow.addView(labelView)
        }

        container.addView(labelsRow)
        return container
    }
}
