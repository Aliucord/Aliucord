package com.aliucord.coreplugins.welcomepage

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.view.*
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.lytefast.flexinput.R
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils
import com.aliucord.views.Divider
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import android.graphics.drawable.Drawable

import android.graphics.drawable.LayerDrawable
import android.transition.TransitionManager


class ClosedDrawable(private val drawable: Drawable) : LayerDrawable(arrayOf(drawable)) {
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
        /*view.findViewById<ViewGroup>(Utils.getResId("action_bar_toolbar", "id")).visibility = View.GONE*/
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

            // break the news to clueless people coming from youtube tutorials LMAO

            val fromYT = LinearLayout(ctx).apply {
                addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).apply { text = "dsfjiknbuhszfjknhsdzfjoiuzsdjio" })
            }

            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).run {
                text = "IF YOU CAME FROM A YOUTUBE TUTORIAL"
                gravity = Gravity.CENTER
                compoundDrawablePadding = DimenUtils.dpToPx(4)
                setCompoundDrawablesRelativeWithIntrinsicBounds(open, null, null, null)
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_bold)
                setTextColor(ColorCompat.getColor(ctx, R.c.uikit_btn_bg_color_selector_red))
                textSize = 16f
                setOnClickListener {
                    TransitionManager.beginDelayedTransition(fromYT)
                    if (fromYT.visibility != View.VISIBLE) {
                        fromYT.visibility = View.VISIBLE
                    } else {
                        fromYT.visibility = View.GONE
                    }
                }
                addView(this)
            }
            addView(fromYT)

            // how to find & install plugins - explain that plugin downloader is built in

            // how to install themes

            // explain how to request a plugin
        }

        addView(layout)
    }
}
