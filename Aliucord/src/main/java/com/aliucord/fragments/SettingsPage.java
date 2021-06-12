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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setActionBarSubtitle("Aliucord");
        setActionBarDisplayHomeAsUpEnabled();
    }

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);

        // Clear page layout
        ((LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0)).removeAllViews();
    }
}
