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
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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
import com.discord.api.utcdatetime.UtcDateTime
import com.discord.stores.StoreStream
import com.discord.utilities.images.MGImages
import com.facebook.drawee.view.SimpleDraweeView

internal data class SafetyHubResponse(
    @SerializedName("account_standing")
    val accountStanding: AccountStandingState,
    val classifications: List<UserClassifications>
) {
    data class UserClassifications(
        val id: Long,
        val description: String,
        @SerializedName("flagged_content")
        val flaggedContent: List<FlaggedContent>?,
        val actions: List<Actions>?,
        @SerializedName("max_expiration_time")
        val maxExpirationTime: UtcDateTime
    )

    data class Actions(val descriptions: List<String>)
    data class FlaggedContent(val content: String?)
    data class AccountStandingState(private val state: Int) {
        val headerString: String
            get() = when (state) {
                100 -> "Your account is all good"
                200 -> "Your account is limited"
                300 -> "Your account is very limited."
                400 -> "Your account is at risk"
                500 -> "Your account is suspended."
                else -> "Unknown"
            }

        val bodyString: String
            get() = when (state) {
                100 -> "Thank you for upholding Discord's Terms of Service and Community Guidelines. If you break the rules, it will show up here."
                200 -> "You may lose access to some parts of Discord if you break the rules again."
                300 -> "You can't use some parts of Discord, You may be suspended if you break the rules again."
                400 -> "You broke Discord's rules. You will be permanently suspended if you break them again."
                500 -> "Due to serious policy violations, your account is permanently suspended, You can no longer use Discord."
                else -> "Unknown"
            }

        val status: Int
            get() = state
    }
}

internal data class ClassificationState(val state: Int, val color: Int)

internal class AccountStandingPage : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Account Standing")
        setActionBarSubtitle("User Settings")

        val indicator = FrameLayout(view.context).addTo(linearLayout) {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            ProgressBar(view.context).addTo(this) {
                layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER)
            }
        }

        Utils.threadPool.execute {
            try {
                val json = Http.Request.newDiscordRNRequest("/safety-hub/@me", "GET").execute()
                    .json(GsonUtils.gsonRestApi, SafetyHubResponse::class.java)

                Utils.mainThread.post {
                    SimpleDraweeView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                        val me = StoreStream.getUsers().me
                        layoutParams = LinearLayout.LayoutParams(100.dp, 100.dp).apply {
                            gravity = Gravity.CENTER
                        }
                        MGImages.setImage(this, "https://cdn.discordapp.com/avatars/${me.id}/${me.avatar}.png?size=100")
                        MGImages.setRoundingParams(this, 100f * 100, false, null, null, 0f)
                    }.addTo(linearLayout)

                    TextView(context, null, 0, R.i.UiKit_Settings_Item).apply {
                        text = json.accountStanding.headerString
                        typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                        textSize = 18f
                        gravity = Gravity.CENTER
                        setPadding(0.dp, 0.dp, 0.dp, 0.dp)
                    }.addTo(linearLayout)

                    TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                        text = json.accountStanding.bodyString
                        typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                        textSize = 12f
                        gravity = Gravity.CENTER
                    }.addTo(linearLayout)

                    createIndicator(view.context, SafetyHubResponse.AccountStandingState(json.accountStanding.status)).addTo(linearLayout)

                    if (json.classifications.isNotEmpty()) {
                        TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                            text = "Active, or past violations"
                            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                            textSize = 12f
                            gravity = Gravity.LEFT
                            setPadding(0.dp, 0.dp, 0.dp, 0.dp)
                        }.addTo(linearLayout)

                        json.classifications.forEach { classification ->
                            val actions = classification.actions?.firstOrNull()?.descriptions ?: listOf("Not provided")
                            val message = classification.flaggedContent?.firstOrNull()?.content ?: "Not provided"

                            ViolationCard(view.context,
                                classification.description,
                                message,
                                actions,
                                classification.id,
                                classification.maxExpirationTime
                            ).addTo(linearLayout)
                        }
                    }

                    removeView(indicator)
                }
            } catch (e: Exception) {
                Logger("AccountStanding").errorToast("Failed to check account standing", e)
            }
        }
    }

    private fun createIndicator(context: Context, currentState: SafetyHubResponse.AccountStandingState): LinearLayout {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 24.dp, 24.dp, 24.dp)
        }

        val states = arrayOf(
            ClassificationState(100, Utils.appContext.getColor(R.c.uikit_btn_bg_color_selector_green)),
            ClassificationState(200, Utils.appContext.getColor(R.c.status_yellow)),
            ClassificationState(300, Utils.appContext.getColor(R.c.status_orange)),
            ClassificationState(400, Utils.appContext.getColor(R.c.uikit_btn_bg_color_selector_red)),
            ClassificationState(500, Utils.appContext.getColor(R.c.uikit_btn_bg_color_selector_red))
        )

        val indicator = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        states.forEachIndexed { index, (state, color) ->
            FrameLayout(context).apply {
                layoutParams = if (currentState.status == state) LinearLayout.LayoutParams(21.dp, 21.dp) else LinearLayout.LayoutParams(16.dp, 16.dp)
                val circleDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(if (currentState.status == state) color else Utils.appContext.getColor(R.c.status_grey_200))
                }
                background = circleDrawable
            }.addTo(indicator)

            if (index < states.size - 1) {
                View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 4.dp, 1f)
                    setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorPrimaryDivider))
                }.addTo(indicator)
            }
        }

        container.addView(indicator)
        return container
    }
}
