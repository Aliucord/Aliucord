/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.fragments;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Utils;
import com.discord.app.AppFragment;
import com.google.android.material.appbar.AppBarLayout;

@SuppressWarnings("unused")
public class SettingsPage extends AppFragment {
    private static final int resId = Utils.getResId("widget_settings_behavior", "layout");
    private CoordinatorLayout view;
    private LinearLayout layout;
    private Toolbar toolbar;

    @SuppressLint("ResourceType")
    public SettingsPage() {
        super(resId);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onViewBound(View view) {
        super.onViewBound(view);
        this.view = (CoordinatorLayout) view;

        setActionBarSubtitle("Aliucord");
        setActionBarDisplayHomeAsUpEnabled();

        clear();
        setPadding(Utils.getDefaultPadding());
    }

    /** Returns the LinearLayout associated with this Page */
    public final LinearLayout getLinearLayout() {
        if (layout == null) {
            if (view == null) throw new IllegalStateException("This Page has not been initialised yet. Did you forget to call super.onViewBound?");
            layout = (LinearLayout) ((NestedScrollView) view.getChildAt(1)).getChildAt(0);
        }
        return layout;
    }

    /** Returns the Toolbar associated with this Page */
    public final Toolbar getHeaderBar() {
        if (toolbar == null) {
            if (view == null) throw new IllegalStateException("This Page has not been initialised yet. Did you forget to call super.onViewBound?");
            toolbar = (Toolbar) ((AppBarLayout) view.getChildAt(0)).getChildAt(0);
        }
        return toolbar;
    }

    /** Sets the padding of the LinearLayout associated with this Page */
    public final void setPadding(int p) {
        getLinearLayout().setPadding(p, p, p, p);
    }

    /** Removes a button from the Toolbar associated with this Page */
    public final void addHeaderButton(AppCompatImageButton button) {
        getHeaderBar().addView(button);
    }

    /** Adds a button to the Toolbar associated with this Page */
    public final void removeHeaderButton(AppCompatImageButton button) {
        getHeaderBar().removeView(button);
    }

    /** Adds a view to the LinearLayout associated with this Page */
    public final void addView(View view) {
        getLinearLayout().addView(view);
    }

    /** Removes a view from the LinearLayout associated with this Page */
    public final void removeView(View view) {
        getLinearLayout().removeView(view);
    }

    /** Removes all views from the LinearLayout associated with this Page */
    public final void clear() {
        getLinearLayout().removeAllViews();
    }

    /** Removes all views from the LinearLayout associated with this Page and calls onViewBound */
    public final void reRender() {
        clear();
        onViewBound(requireView());
    }

    /** Closes this SettingsPage by simulating a back press */
    public final void close() {
        requireActivity().onBackPressed();
    }
}
