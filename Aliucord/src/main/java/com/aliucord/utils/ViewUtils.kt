package com.aliucord.utils

import android.view.View
import android.view.ViewGroup

object ViewUtils {
    /**
     * Shorthand extension function to add a View into a ViewGroup
     *
     * @param group ViewGroup to add this View into
     * @return The View
     */
    fun <T : View> T.addTo(group: ViewGroup): T = apply { group.addView(this) }

    /**
     * Shorthand extension function to add a View into a ViewGroup, and then
     * run a scoped function
     *
     * @param group ViewGroup to add this View into
     * @param block A scoped function, with the View as its receiver
     * @return The View
     */
    fun <T : View> T.addTo(group: ViewGroup, block: T.() -> Unit): T = apply { block(); group.addView(this) }

    /**
     * Shorthand extension function to add a View into a ViewGroup at specified
     * index, and then run a scoped function
     *
     * @param group ViewGroup to add this View into
     * @param index Index to insert this View at
     * @param block A scoped function, with the View as its receiver
     * @return The View
     */
    fun <T : View> T.addTo(group: ViewGroup, index: Int, block: T.() -> Unit): T = apply { block(); group.addView(this, index) }
}
