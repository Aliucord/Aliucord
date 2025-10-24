package com.aliucord.utils

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.CompoundButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliucord.Utils
import com.aliucord.utils.ViewUtils.findViewById
import com.discord.views.CheckedSetting

object ViewUtils {
    /**
     * Shorthand extension function to add a View into a ViewGroup, and then
     * run a scoped function
     *
     * @param group ViewGroup to add this View into
     * @param block A scoped function, with the View as its receiver
     * @return The View
     */
    inline fun <T : View> T.addTo(group: ViewGroup, block: T.() -> Unit = {}): T {
        block()
        group.addView(this)
        return this
    }

    /**
     * Shorthand extension function to add a View into a ViewGroup at specified
     * index, and then run a scoped function
     *
     * @param group ViewGroup to add this View into
     * @param index Index to insert this View at
     * @param block A scoped function, with the View as its receiver
     * @return The View
     */
    inline fun <T : View> T.addTo(group: ViewGroup, index: Int, block: T.() -> Unit = {}): T {
        block()
        group.addView(this, index)
        return this
    }

    /**
     * The same as [View.findViewById], but takes the name of the id instead.
     *
     * @param idName the name of the id resource
     *
     * @return a view with the given id in the layout, or `null` if it is not found
     */
    fun <T : View?> View.findViewById(idName: String): T {
        val id = Utils.getResId(idName, "id")
        return findViewById<T>(id)
    }

    /**
     * Adds default discord paddings as margins to a View. By default, margins are set for the
     * bottom, left, and right of the View, but not the top.
     *
     * @param bottom Whether to set topMargin. Default is true.
     * @param top Whether to set bottomMargin. Default is false.
     * @param left Whether to set leftMargin. Default is true.
     * @param right Whether to set rightMargin. Default is true.
     *
     * @return The View
     */
    fun <T : View> T.setDefaultMargins(
        bottom: Boolean = true,
        top: Boolean = false,
        left: Boolean = true,
        right: Boolean = true,
    ): T {
        val p = DimenUtils.defaultPadding
        val params = if (layoutParams == null) {
            MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT)
        } else {
            MarginLayoutParams(layoutParams)
        }
        layoutParams = params.apply {
            if (top) topMargin = p
            if (bottom) bottomMargin = p
            if (left) leftMargin = p
            if (right) rightMargin = p
        }
        return this
    }

    /** Main layout of the setting */
    val CheckedSetting.layout get() = l.b() as ConstraintLayout

    /** Main text/label of the setting */
    val CheckedSetting.label get() = l.a() as TextView

    /** Checkbox button at the end of the setting */
    val CheckedSetting.checkbox get() = l.c() as CompoundButton

    /** Subtext of the setting */
    val CheckedSetting.subtext get() = l.f() as TextView
}
