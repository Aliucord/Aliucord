package com.aliucord.coreplugins.accountstanding

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
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

                            ViolationCard(view.context,
                                classification.description,
                                message,
                                actions,
                                classification.id,
                                classification.maxExpirationTime).addTo(linearLayout)
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

    private fun accountStandingStatus(context: Context, currentStanding: AccountStanding) = ConstraintLayout(context).also { layout ->
        layout.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

        val states = AccountStanding.entries - AccountStanding.UNKNOWN
        val dotIds = IntArray(states.size) { View.generateViewId() }
        val labelIds = IntArray(states.size) { View.generateViewId() }
        val dividerIds = IntArray(states.lastIndex) { View.generateViewId() }

        states.forEachIndexed { index, standing ->
            val isCurrent = standing == currentStanding

            layout.addView(View(context).apply {
                id = dotIds[index]
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(if (isCurrent) currentStanding.color else Utils.appContext.getColor(R.c.status_grey_200))
                }
            }, ConstraintLayout.LayoutParams(16.dp, 16.dp))

            layout.addView(TextView(context).apply {
                id = labelIds[index]
                text = standing.status
                typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                textSize = 8f
                setTextColor(ColorCompat.getThemedColor(context, R.b.primary_300))
            }, ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

            if (index < states.lastIndex) {
                layout.addView(View(context).apply {
                    id = dividerIds[index]
                    setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorPrimaryDivider))
                }, ConstraintLayout.LayoutParams(0, 4.dp))
            }
        }

        ConstraintSet().apply {
            clone(layout)

            connect(dotIds[0], ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            createHorizontalChain(
                ConstraintSet.PARENT_ID, ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
                dotIds, null,
                ConstraintSet.CHAIN_SPREAD_INSIDE,
            )

            dividerIds.forEachIndexed { index, id ->
                connect(id, ConstraintSet.START, dotIds[index], ConstraintSet.END)
                connect(id, ConstraintSet.END, dotIds[index + 1], ConstraintSet.START)
                connect(id, ConstraintSet.TOP, dotIds[0], ConstraintSet.TOP)
                connect(id, ConstraintSet.BOTTOM, dotIds[0], ConstraintSet.BOTTOM)
            }

            labelIds.forEachIndexed { index, id ->
                connect(id, ConstraintSet.TOP, dotIds[index], ConstraintSet.BOTTOM, 4.dp)
                when (index) {
                    0 -> connect(id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                    states.lastIndex -> connect(id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                    else -> {
                        connect(id, ConstraintSet.START, dotIds[index], ConstraintSet.START)
                        connect(id, ConstraintSet.END, dotIds[index], ConstraintSet.END)
                    }
                }
            }

            applyTo(layout)
        }
    }
}
