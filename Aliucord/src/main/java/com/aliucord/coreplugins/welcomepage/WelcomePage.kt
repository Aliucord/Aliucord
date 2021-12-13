package com.aliucord.coreplugins.welcomepage

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.view.*
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.lytefast.flexinput.R
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils
import com.aliucord.views.Divider
import com.discord.utilities.color.ColorCompat
import android.graphics.drawable.Drawable

import android.graphics.drawable.LayerDrawable
import android.transition.TransitionManager
import com.aliucord.*
import com.aliucord.coreplugins.CorePlugins
import com.aliucord.views.Button
import com.discord.stores.StoreInviteSettings
import com.discord.widgets.guilds.invite.WidgetGuildInvite


private class ClosedDrawable(private val drawable: Drawable) : LayerDrawable(arrayOf(drawable)) {
    override fun draw(canvas: Canvas) {
        val bounds = drawable.bounds
        canvas.save()
        canvas.rotate(270F, bounds.width() / 2f, bounds.height() / 2f)
        super.draw(canvas)
        canvas.restore()
    }
}


class WelcomePage : SettingsPage() {

    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        val ctx = requireContext()
        view.findViewById<ViewGroup>(Utils.getResId("action_bar_toolbar", "id")).visibility = View.GONE
        setPadding(0)

        val layout = LinearLayout(ctx).apply {
            dividerPadding = DimenUtils.defaultPadding
            gravity = Gravity.TOP
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                orientation = LinearLayout.VERTICAL
            }
        }

        val open = ContextCompat.getDrawable(ctx, R.e.ic_arrow_down_14dp)?.apply {
            mutate()
            setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal))
        }

        val closed = ClosedDrawable(open!!)

        val warning = ContextCompat.getDrawable(ctx, R.e.ic_warning_circle_24dp)?.apply {
            mutate()
        }

        with(layout) {
            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).run {
                text = "Welcome to Aliucord"
                typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_bold)
                textSize = 16f
                gravity = Gravity.CENTER
                addView(this)
            }

            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).run {
                text = "Tap a topic below to learn more about it"
                typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
                textSize = 16f
                gravity = Gravity.CENTER
                addView(this)
            }

            addView(Divider(ctx).apply { scaleY = 5f })


            val fromYT = LinearLayout(ctx).apply {
                addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                    gravity = Gravity.CENTER
                    text = "If you were promised \"free nitro\" or anything like that, you have been LIED TO."
                    textSize = 16f
                    compoundDrawablePadding = DimenUtils.dpToPx(4)
                    setTextColor(ColorCompat.getColor(ctx, R.c.premium_perk_orange))
                    setCompoundDrawablesRelativeWithIntrinsicBounds(warning, null, null, null)
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        orientation = LinearLayout.VERTICAL
                    }
                })

                addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                    gravity = Gravity.CENTER
                    text = """
                    • Plugin Downloader is built in. There is no need to install it separately.
                    • Plugins for PC client mods DO NOT WORK on Aliucord, duh.
                    • Support Server: The development, plugin-request discussion, and offtopic channels are NOT support channels.
                """.trimIndent()
                    textSize = 16f
                })
                dividerPadding = DimenUtils.defaultPadding
                gravity = Gravity.CENTER
                visibility = View.GONE
            }

            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).run {
                text = "READ THIS FIRST!"
                gravity = Gravity.CENTER
                compoundDrawablePadding = DimenUtils.dpToPx(4)
                setCompoundDrawablesRelativeWithIntrinsicBounds(closed, null, null, null)
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_bold)
                setTextColor(ColorCompat.getColor(ctx, R.c.uikit_btn_bg_color_selector_red))
                textSize = 16f
                setOnClickListener {
                    TransitionManager.beginDelayedTransition(fromYT)
                    fromYT.visibility = if (fromYT.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    setCompoundDrawablesRelativeWithIntrinsicBounds(if (fromYT.visibility == View.VISIBLE) open else closed, null, null, null)
                }
                addView(this)
            }
            addView(fromYT)
            addView(Divider(ctx))

            val installPlugins = LinearLayout(ctx).apply {
                addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                    gravity = Gravity.CENTER
                    text = "PluginDownloader is built in! You don't need to do anything extra."
                    textSize = 16f
                    compoundDrawablePadding = DimenUtils.dpToPx(4)
                    setTextColor(ColorCompat.getColor(ctx, R.c.premium_perk_orange))
                    setCompoundDrawablesRelativeWithIntrinsicBounds(warning, null, null, null)
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        orientation = LinearLayout.VERTICAL
                    }
                })

                addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                    gravity = Gravity.CENTER
                    text = """
                        0. Join the Aliucord Server below.
                        1. Navigate to #plugins-list
                        2. Long-press (hold down on) a message until a context menu pops up
                        3. Press "Open PluginDownloader", then install a plugin from there.
                        3.5. Most plugins require a restart to fully start working, so you may see a prompt to restart.
                """.trimIndent()
                    textSize = 16f
                })
                dividerPadding = DimenUtils.defaultPadding
                gravity = Gravity.CENTER
                visibility = View.GONE
            }

            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).run {
                text = "How to install Plugins"
                gravity = Gravity.CENTER
                compoundDrawablePadding = DimenUtils.dpToPx(4)
                setCompoundDrawablesRelativeWithIntrinsicBounds(closed, null, null, null)
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_bold)
                setTextColor(ColorCompat.getColor(ctx, R.c.brand_new))
                textSize = 16f
                setOnClickListener {
                    TransitionManager.beginDelayedTransition(installPlugins)
                    installPlugins.visibility = if (installPlugins.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    setCompoundDrawablesRelativeWithIntrinsicBounds(if (installPlugins.visibility == View.VISIBLE) open else closed, null, null, null)
                }
                addView(this)
            }
            addView(installPlugins)
            addView(Divider(ctx))


            val installThemes = LinearLayout(ctx).apply {
                addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                    gravity = Gravity.CENTER
                    text =
                        "The Themer plugin is required to download and use themes. Follow the \"How to install plugins\" guide if you don't know how to install it."
                    textSize = 16f
                    compoundDrawablePadding = DimenUtils.dpToPx(4)
                    setTextColor(ColorCompat.getColor(ctx, R.c.premium_perk_orange))
                    setCompoundDrawablesRelativeWithIntrinsicBounds(warning, null, null, null)
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        orientation = LinearLayout.VERTICAL
                    }
                })

                addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                    gravity = Gravity.CENTER
                    text = """
                        0. Join the Aliucord Server below.
                        1. Navigate to #themes
                        2. Long-press (hold down on) a message until a context menu pops up
                        3. Press "Install [Theme Name]".
                        4. Enable it from Themer settings, if not already enabled.
                        4.5. You may want to enable transparency, custom fonts, or custom sounds for the best effect.
                """.trimIndent()
                    textSize = 16f
                })
                dividerPadding = DimenUtils.defaultPadding
                gravity = Gravity.CENTER
                visibility = View.GONE
            }

            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).run {
                text = "How to install Themes"
                gravity = Gravity.CENTER
                compoundDrawablePadding = DimenUtils.dpToPx(4)
                setCompoundDrawablesRelativeWithIntrinsicBounds(closed, null, null, null)
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_bold)
                setTextColor(ColorCompat.getColor(ctx, R.c.brand_new))
                textSize = 16f
                setOnClickListener {
                    TransitionManager.beginDelayedTransition(installThemes)
                    installThemes.visibility = if (installThemes.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    setCompoundDrawablesRelativeWithIntrinsicBounds(if (installThemes.visibility == View.VISIBLE) open else closed, null, null, null)
                }
                addView(this)
            }
            addView(installThemes)
            addView(Divider(ctx))

            val requestPlugin = LinearLayout(ctx).apply {
                addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                    gravity = Gravity.CENTER
                    isAllCaps = false
                    text = "Please search in #plugin-list to make sure the plugin doesn't already exist first."
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        orientation = LinearLayout.VERTICAL
                    }
                    setTextColor(ColorCompat.getColor(ctx, R.c.premium_perk_orange))
                    setCompoundDrawablesRelativeWithIntrinsicBounds(warning, null, null, null)
                })

                addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                    gravity = Gravity.CENTER
                    text = """
                        0. Join the Aliucord Server below.
                        1. Navigate to #plugin-request-discussion (look for something named closely, the name likely changed)
                        2. Go to the pinned messages and READ THEM!
                        3. Follow the instructions in the pinned message & create a plugin request on the GitHub.
                        USE SEARCH BEFORE REQUESTING so you don't make a duplicate request!
                """.trimIndent()
                    textSize = 16f
                })
                dividerPadding = DimenUtils.defaultPadding
                gravity = Gravity.CENTER
                visibility = View.GONE
            }

            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).run {
                text = "Requesting a NEW plugin"
                gravity = Gravity.CENTER
                compoundDrawablePadding = DimenUtils.dpToPx(4)
                setCompoundDrawablesRelativeWithIntrinsicBounds(closed, null, null, null)
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_bold)
                setTextColor(ColorCompat.getColor(ctx, R.c.brand_new))
                textSize = 16f
                setOnClickListener {
                    TransitionManager.beginDelayedTransition(requestPlugin)
                    requestPlugin.visibility = if (requestPlugin.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    setCompoundDrawablesRelativeWithIntrinsicBounds(if (requestPlugin.visibility == View.VISIBLE) open else closed, null, null, null)
                }
                addView(this)
            }
            addView(requestPlugin)
            addView(Divider(ctx).apply { scaleY = 5f })
            val padding = DimenUtils.defaultPadding

            addView(Button(ctx).apply {
                text = "Aliucord Support Server"
                isAllCaps = false
                setPadding(padding)
                setOnClickListener { (WidgetGuildInvite.Companion).launch(ctx, StoreInviteSettings.InviteCode(Constants.ALIUCORD_SUPPORT, "", null)) }
            })

            addView(Button(ctx).apply {
                text = "I understand, close this!"
                setPadding(padding)
                setOnClickListener {
                    close()
                    PluginManager.plugins["WelcomePage"]?.settings?.setBool("hasShownWelcome", true)
                }
            })
        }
        addView(layout)
        setOnBackPressed {
            PluginManager.plugins["WelcomePage"]?.settings?.setBool("hasShownWelcome", true)
            return@setOnBackPressed false
        }
    }
}
