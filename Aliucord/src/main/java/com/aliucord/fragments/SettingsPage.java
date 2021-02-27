package com.aliucord.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;

import com.aliucord.Main;
import com.aliucord.Utils;
import com.discord.app.AppActivity;
import com.discord.app.AppFragment;

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
        u.m.c.j.checkNotNullParameter(view, "view");
        super.onViewBound(view);

        // Clear page layout
        ((LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0)).removeAllViews();
    }
}
