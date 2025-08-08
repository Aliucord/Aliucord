package com.aliucord.utils

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
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
    fun <T : View> T.addTo(group: ViewGroup, block: (T.() -> Unit)? = null): T = apply { block?.invoke(this); group.addView(this) }

    /**
     * Shorthand extension function to add a View into a ViewGroup at specified
     * index, and then run a scoped function
     *
     * @param group ViewGroup to add this View into
     * @param index Index to insert this View at
     * @param block A scoped function, with the View as its receiver
     * @return The View
     */
    fun <T : View> T.addTo(group: ViewGroup, index: Int, block: (T.() -> Unit)? = null): T = apply { block?.invoke(this); group.addView(this, index) }

    /** Main layout of the setting */
    val CheckedSetting.layout get() = l.b() as ConstraintLayout

    /** Main text/label of the setting */
    val CheckedSetting.label get() = l.a()

    /** Checkbox button at the end of the setting */
    val CheckedSetting.checkbox get() = l.c()

    /** Subtext of the setting */
    val CheckedSetting.subtext get() = l.f()
}
