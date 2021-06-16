/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Utils;
import com.discord.app.AppFragment;

public class SettingsPage extends AppFragment {
    @SuppressLint("ResourceType")
    public SettingsPage() { super(Utils.getResId("widget_settings_behavior", "layout")); }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onViewBound(View view) {
        super.onViewBound(view);
        setActionBarSubtitle("Aliucord");
        setActionBarDisplayHomeAsUpEnabled();

        // Clear page layout
        ((LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0)).removeAllViews();
    }
}
