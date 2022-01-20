/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Utils;
import com.aliucord.utils.DimenUtils;
import com.aliucord.views.ToolbarButton;
import com.discord.app.AppFragment;
import com.google.android.material.appbar.AppBarLayout;


/** Settings Page Fragment */
@SuppressWarnings({ "deprecation", "unused" })
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
    public void onViewBound(View view) {
        super.onViewBound(view);
        this.view = (CoordinatorLayout) view;

        setActionBarSubtitle("Aliucord");
        setActionBarDisplayHomeAsUpEnabled();

        clear();
        getHeaderBar().getMenu().clear();
        setPadding(DimenUtils.getDefaultPadding());
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

    /**
     * Add a button to the header {@link Toolbar} of this page
     *
     * @param id       The id of this button
     * @param order    The order to show this button in. See {@link MenuItem#getOrder()}
     * @param title    The title of this button
     * @param drawable The drawable this button should have
     * @param onClick  The onClick listener of this button
     * @return The id of this header button
     * @see Toolbar#getMenu()
     * @see Menu#add(int, int, int, CharSequence)
     */
    public final int addHeaderButton(int id, int order, String title, Drawable drawable, MenuItem.OnMenuItemClickListener onClick) {
        getHeaderBar().getMenu()
            .add(Menu.NONE, id, order, title)
            .setIcon(drawable)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            .setOnMenuItemClickListener(onClick);
        return id;
    }

    /**
     * Add a button to the header {@link Toolbar} of this page
     *
     * @param id       The id of this button
     * @param title    The title of this button
     * @param drawable The drawable this button should have
     * @param onClick  The onClick listener of this button
     * @return The id of this header button
     * @see Toolbar#getMenu()
     * @see Menu#add(int, int, int, CharSequence)
     */
    public final int addHeaderButton(int id, String title, Drawable drawable, MenuItem.OnMenuItemClickListener onClick) {
        return addHeaderButton(id, Menu.NONE, title, drawable, onClick);
    }

    /**
     * Add a button to the header {@link Toolbar} of this page
     *
     * @param title    The title of this button
     * @param drawable The drawable this button should have
     * @param onClick  The onClick listener of this button
     * @return The id of this header button
     * @see Toolbar#getMenu()
     * @see Menu#add(int, int, int, CharSequence)
     */
    public final int addHeaderButton(String title, Drawable drawable, MenuItem.OnMenuItemClickListener onClick) {
        return addHeaderButton(View.generateViewId(), title, drawable, onClick);
    }

    /**
     * Add a button to the header {@link Toolbar} of this page
     *
     * @param title      The title of this button
     * @param drawableId The id of the drawable this button should have
     * @param onClick    The onClick listener of this button
     * @return The id of this header button
     * @see Toolbar#getMenu()
     * @see Menu#add(int, int, int, CharSequence)
     */
    public final int addHeaderButton(String title, @DrawableRes int drawableId, MenuItem.OnMenuItemClickListener onClick) {
        return addHeaderButton(View.generateViewId(), title, ContextCompat.getDrawable(Utils.getAppContext(), drawableId), onClick);
    }

    /**
     * Adds a button from the Toolbar associated with this Page
     *
     * @deprecated Use {@link #addHeaderButton(String, Drawable, MenuItem.OnMenuItemClickListener)}
     * or {@link #getHeaderBar()}.getMenu().add(...)
     */
    @Deprecated
    public final void addHeaderButton(ToolbarButton button) {
        getHeaderBar().addView(button);
    }

    /**
     * Removes a button to the Toolbar associated with this Page
     *
     * @deprecated Use {@link #removeHeaderButton(int)} with id returned by {@link #addHeaderButton(String, Drawable, MenuItem.OnMenuItemClickListener)}
     */
    @Deprecated
    public final void removeHeaderButton(ToolbarButton button) {
        getHeaderBar().removeView(button);
    }

    public final void removeHeaderButton(int id) {
        getHeaderBar().getMenu().removeItem(id);
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
        getHeaderBar().getMenu().clear();
        onViewBound(view);
    }

    /** Closes this SettingsPage by simulating a back press */
    public final void close() {
        requireActivity().onBackPressed();
    }

    @Override
    public final Context getContext() {
        return view != null ? view.getContext() : super.getContext();
    }
}
