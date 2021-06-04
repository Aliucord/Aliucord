/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.views.Button;
import com.aliucord.views.Divider;
import com.aliucord.widgets.UpdaterPluginCard;
import com.lytefast.flexinput.R$h;

public class Updater extends SettingsPage {
    private final Logger logger = new Logger("Updater");
    private String stateText = "No new updates found";

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setActionBarTitle("Updater");
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onViewBound(View view) {
        super.onViewBound(view);

        Context context = requireContext();
        int padding = Utils.getDefaultPadding();
        LinearLayout v = (LinearLayout) ((NestedScrollView) ((CoordinatorLayout)
                view).getChildAt(1)).getChildAt(0);
        v.setPadding(padding, padding, padding, padding);

        TextView state = new TextView(context, null, 0, R$h.UiKit_Settings_Item_SubText);
        state.setText(stateText);
        state.setPadding(0, padding / 2, 0, padding / 2);

        LinearLayout buttons = new LinearLayout(context);

        Runnable forceUpdate = () -> {
            v.removeAllViews();
            onViewBound(view);
        };

        Button btn = new Button(context, false);
        btn.setText("Check for Updates");
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, padding, 0);
        btn.setLayoutParams(layoutParams);
        btn.setOnClickListener(e -> {
            state.setText("Checking for updates...");
            new Thread(() -> {
                PluginUpdater.checkUpdates(false);
                Utils.mainThread.post(forceUpdate);
                int updateCount = PluginUpdater.updates.size();
                if (updateCount == 0)
                    stateText = "No updates found";
                else
                    stateText = String.format("Found %s", Utils.pluralise(updateCount, "update"));
                Utils.mainThread.post(() -> state.setText(stateText));
            }).start();
        });
        buttons.addView(btn);

        btn = new Button(context, false);
        btn.setText("Update All");
        btn.setOnClickListener(e -> {
            state.setText("Updating...");
            new Thread(() -> {
                int updateCount = PluginUpdater.updateAll();
                if (updateCount == 0) {
                    stateText = "No updates found";
                } else if (updateCount == -1) {
                     stateText = "Something went wrong while updating. Please try again";
                } else {
                    stateText = String.format("Successfully updated %s!", Utils.pluralise(updateCount, "plugin"));
                }
                Utils.mainThread.post(forceUpdate);
            }).start();
        });
        buttons.addView(btn);
        v.addView(buttons);
        v.addView(state);

        int updateCount = PluginUpdater.updates.size();
        if (updateCount == 0) return;
        stateText = String.format("Found %s", Utils.pluralise(updateCount, "update"));
        state.setText(stateText);
        v.addView(new Divider(context));

        for (String plugin : PluginUpdater.updates) v.addView(new UpdaterPluginCard(context, plugin, forceUpdate));
    }
}
