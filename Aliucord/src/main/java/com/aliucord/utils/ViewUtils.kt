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

data class Padding(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    operator fun plus(other: Padding) = Padding(
        left + other.left,
        top + other.top,
        right + other.right,
        bottom + other.bottom,
    )

    operator fun plus(other: Int) = Padding(
        left + other,
        top + other,
        right + other,
        bottom + other,
    )

    operator fun minus(other: Padding) = Padding(
        left - other.left,
        top - other.top,
        right - other.right,
        bottom - other.bottom,
    )

    operator fun minus(other: Int) = Padding(
        left - other,
        top - other,
        right - other,
        bottom - other,
    )

    operator fun times(other: Int) = Padding(
        left * other,
        top * other,
        right * other,
        bottom * other,
    )

    operator fun div(other: Int) = Padding(
        left / other,
        top / other,
        right / other,
        bottom / other,
    )
}

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

    // Padding utilities

    /** Set a uniform padding for all sides */
    fun View.setPadding(value: Int) = setPadding(value, value, value, value)

    /** Convenient property to get and set left padding */
    inline var View.leftPadding
        get() = paddingLeft
        set(value) = setPadding(value, paddingTop, paddingRight, paddingBottom)

    /** Convenient property to get and set top padding */
    inline var View.topPadding
        get() = paddingTop
        set(value) = setPadding(paddingLeft, value, paddingRight, paddingBottom)

    /** Convenient property to get and set right padding */
    inline var View.rightPadding
        get() = paddingRight
        set(value) = setPadding(paddingLeft, paddingTop, value, paddingBottom)

    /** Convenient property to get and set bottom padding */
    inline var View.bottomPadding
        get() = paddingBottom
        set(value) = setPadding(paddingLeft, paddingTop, paddingRight, value)

    /** Convenient property to get and set start padding (left on ltr, right on rtl) */
    inline var View.startPadding
        get() = paddingStart
        set(value) = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            rightPadding = value
        } else {
            leftPadding = value
        }

    /** Convenient property to get and set end padding (right on ltr, left on rtl) */
    inline var View.endPadding
        get() = paddingEnd
        set(value) = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            leftPadding = value
        } else {
            rightPadding = value
        }

    /** Convenient property to get and set a [Padding] value */
    inline var View.padding
        get() = Padding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        set(value) = with(value) { setPadding(left, top, right, bottom) }
}
