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
import com.aliucord.coreplugins.accountstanding.AccountStandingPageResponse.AccountStanding
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.setPadding
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.icon.IconUtils
import com.discord.utilities.images.MGImages
import com.facebook.drawee.view.SimpleDraweeView
import com.lytefast.flexinput.R

internal class AccountStandingPage : SettingsPage() {
    private val logger = Logger("AccountStanding")

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Account Standing")
        setActionBarSubtitle("User Settings")

        val loadingIndicator = FrameLayout(view.context).addTo(linearLayout) {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            ProgressBar(view.context).addTo(this) {
                layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER)
            }
        }

        Utils.threadPool.execute {
            try {
                val accountStandingResponse = Http.Request.newDiscordRNRequest("/safety-hub/@me", "GET").execute()
                    .json(GsonUtils.gsonRestApi, AccountStandingPageResponse::class.java)

                Utils.mainThread.post {
                    val accountStanding = AccountStanding.from(accountStandingResponse.accountStanding.state)

                    SimpleDraweeView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                        layoutParams = LinearLayout.LayoutParams(100.dp, 100.dp).apply {
                            gravity = Gravity.CENTER
                        }

                        MGImages.setImage(this, IconUtils.getForUser(StoreStream.getUsers().me))
                        MGImages.setRoundingParams(this, 100 / 2f, false, null, null, 0f)
                    }.addTo(linearLayout)

                    TextView(context, null, 0, R.i.UiKit_Settings_Item).apply {
                        text = accountStanding.header
                        typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                        textSize = 18f
                        gravity = Gravity.CENTER
                        setPadding(0)
                    }.addTo(linearLayout)

                    TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                        text = accountStanding.body
                        typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                        textSize = 12f
                        gravity = Gravity.CENTER
                    }.addTo(linearLayout)

                    accountStandingStatus(view.context, accountStanding).addTo(linearLayout)

                    if (accountStandingResponse.classifications.isNotEmpty()) {
                        TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                            text = "Active, or past violations"
                            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                            textSize = 12f
                            gravity = Gravity.LEFT
                            setPadding(0)
                        }.addTo(linearLayout)

                        accountStandingResponse.classifications.forEach { classification ->
                            val actions = classification.actions?.firstOrNull()?.descriptions ?: listOf("Not provided")
                            val message = classification.flaggedContent?.firstOrNull()?.content ?: "Not provided"

                            ViolationCard(
                                view.context,
                                classification.description,
                                message,
                                actions,
                                classification.id,
                                classification.maxExpirationTime
                            ).addTo(linearLayout)
                        }
                    }

                    removeView(loadingIndicator)
                }
            } catch (e: Exception) {
                logger.errorToast("Failed to check account standing", e)
                Utils.mainThread.post { close() }
            }
        }
    }

    private fun accountStandingStatus(context: Context, currentStanding: AccountStanding) = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val states = AccountStanding.entries.filterNot { it == AccountStanding.UNKNOWN }
        states.forEachIndexed { index, standing ->
            val isCurrent = standing == currentStanding
            val iconSize = if (isCurrent) 21.dp else 16.dp
            val iconColor = if (isCurrent) currentStanding.color else Utils.appContext.getColor(R.c.status_grey_200)

            FrameLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(iconColor)
                }
            }.addTo(this)

            if (index < states.lastIndex) {
                View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 4.dp, 1f)
                    setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorPrimaryDivider))
                }.addTo(this)
            }
        }
    }
}
