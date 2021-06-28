/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.widgets;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.widget.NestedScrollView;

import com.aliucord.Utils;
import com.discord.app.AppBottomSheet;
import com.discord.widgets.channels.WidgetChannelSelector;

@SuppressWarnings("unused")
public class BottomSheet extends AppBottomSheet {
    private static int id = 0;
    private NestedScrollView view;
    private LinearLayout layout;

    @Override
    public int getContentViewResId() {
        if (id == 0) id = new WidgetChannelSelector().getContentViewResId();
        return id;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        this.view = (NestedScrollView) view;

        clear();
        setPadding(Utils.getDefaultPadding());
    }

    /** Returns the LinearLayout associated with this BottomSheet */
    public LinearLayout getLinearLayout() {
        if (layout == null) {
            if (view == null) throw new IllegalStateException("This BottomSheet has not been initialised yet. Did you forget to call super.onViewCreated?");
            layout = (LinearLayout) view.getChildAt(0);
        }
        return layout;
    }

    /** Sets the padding of the LinearLayout associated with this BottomSheet */
    public final void setPadding(int p) {
        getLinearLayout().setPadding(p, p, p, p);
    }

    /** Removes all views of the LinearLayout associated with this BottomSheet */
    public void clear() {
        getLinearLayout().removeAllViews();
    }

    /** Adds a view to the LinearLayout associated with this BottomSheet */
    public final void addView(View view) {
        getLinearLayout().addView(view);
    }

    /** Removes a view from the LinearLayout associated with this BottomSheet */
    public final void removeView(View view) {
        getLinearLayout().removeView(view);
    }
}
