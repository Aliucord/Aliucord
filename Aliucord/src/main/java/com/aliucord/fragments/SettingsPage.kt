/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.fragments

import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.aliucord.Utils.getResId
import com.aliucord.utils.DimenUtils.defaultPadding
import com.aliucord.views.ToolbarButton
import com.discord.app.AppFragment
import com.google.android.material.appbar.AppBarLayout

/** Settings Page Fragment  */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class SettingsPage : AppFragment(resId) {
    private var _view: CoordinatorLayout? = null
    private var layout: LinearLayout? = null
    private var toolbar: Toolbar? = null
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        this._view = view as CoordinatorLayout
        setActionBarSubtitle("Aliucord")
        setActionBarDisplayHomeAsUpEnabled()
        clear()
        setPadding(defaultPadding)
    }

    /** Returns the LinearLayout associated with this Page  */
    val linearLayout: LinearLayout
        get() {
            if (layout == null) {
                checkNotNull(_view) { "This Page has not been initialised yet. Did you forget to call super.onViewBound?" }
                layout = (_view!!.getChildAt(1) as NestedScrollView).getChildAt(0) as LinearLayout
            }
            return layout!!
        }

    /** Returns the Toolbar associated with this Page  */
    val headerBar: Toolbar
        get() {
            if (toolbar == null) {
                checkNotNull(_view) { "This Page has not been initialised yet. Did you forget to call super.onViewBound?" }
                toolbar = (_view!!.getChildAt(0) as AppBarLayout).getChildAt(0) as Toolbar
            }
            return toolbar!!
        }

    /** Sets the padding of the LinearLayout associated with this Page  */
    fun setPadding(p: Int) =
        linearLayout.setPadding(p, p, p, p)

    /** Adds a button from the Toolbar associated with this Page  */
    fun addHeaderButton(button: ToolbarButton) =
        headerBar.addView(button)

    /** Removes a button to the Toolbar associated with this Page  */
    fun removeHeaderButton(button: ToolbarButton) =
        headerBar.removeView(button)

    /** Adds a view to the LinearLayout associated with this Page  */
    fun addView(view: View) =
        linearLayout.addView(view)

    /** Removes a view from the LinearLayout associated with this Page  */
    fun removeView(view: View) =
        linearLayout.removeView(view)

    /** Removes all views from the LinearLayout associated with this Page  */
    fun clear() = linearLayout.removeAllViews()

    /** Removes all views from the LinearLayout associated with this Page and calls onViewBound  */
    fun reRender() {
        clear()
        onViewBound(_view!!)
    }

    /** Closes this SettingsPage by simulating a back press  */
    fun close() = requireActivity().onBackPressed()

    override fun getContext() = _view!!.context ?: super.getContext()

    companion object {
        private val resId = getResId("widget_settings_behavior", "layout")
    }
}
