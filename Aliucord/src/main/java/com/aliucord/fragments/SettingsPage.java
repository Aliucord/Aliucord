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

import c0.y.d.m;

public class SettingsPage extends AppFragment {
    @SuppressLint("ResourceType")
    public SettingsPage() { super(Utils.getResId("widget_settings_behavior", "layout")); }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setActionBarSubtitle("Aliucord");
        setActionBarDisplayHomeAsUpEnabled();
    }

    @Override
    public void onViewBound(View view) {
        m.checkNotNullParameter(view, "view");
        super.onViewBound(view);

        // Clear page layout
        ((LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0)).removeAllViews();
    }
}
