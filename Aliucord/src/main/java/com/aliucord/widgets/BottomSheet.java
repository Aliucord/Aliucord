/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.widgets;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.widget.NestedScrollView;

import com.discord.app.AppBottomSheet;
import com.discord.widgets.channels.WidgetChannelSelector;

public class BottomSheet extends AppBottomSheet {
    private static int id = 0;

    @Override
    public int getContentViewResId() {
        if (id == 0) id = new WidgetChannelSelector().getContentViewResId();
        return id;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        // Clear bottom sheet layout
        ((LinearLayout) ((NestedScrollView) view).getChildAt(0)).removeAllViews();
    }
}
