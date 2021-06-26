/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Utils;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.widgets.UpdaterPluginCard;
import com.google.android.material.appbar.AppBarLayout;
import com.lytefast.flexinput.R$d;
import com.lytefast.flexinput.R$h;

public class Updater extends SettingsPage {
    private static final int id = View.generateViewId();
    private String stateText = "No new updates found";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    @SuppressLint("SetTextI18n")
    public void onViewBound(View view) {
        super.onViewBound(view);

        setActionBarTitle("Updater");
        setActionBarSubtitle(stateText);

        Context context = requireContext();
        int padding = Utils.getDefaultPadding();

        LinearLayout v = (LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0);
        v.setPadding(padding, padding, padding, padding);

        Toolbar toolbar = (Toolbar) ((AppBarLayout) ((CoordinatorLayout) view).getChildAt(0)).getChildAt(0);

        if (toolbar.findViewById(id) == null) {
            int p = padding / 2;

            AppCompatImageButton refreshButton = new AppCompatImageButton(context);
            refreshButton.setId(id);
            AppCompatImageButton updateAllButton = new AppCompatImageButton(context);

            Toolbar.LayoutParams refreshParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
            refreshParams.gravity = Gravity.END;
            refreshButton.setLayoutParams(refreshParams);
            Toolbar.LayoutParams updatePrams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
            updatePrams.gravity = Gravity.END;
            updatePrams.setMarginEnd(p);
            updateAllButton.setLayoutParams(updatePrams);
            refreshButton.setPadding(p, p, p, p);
            updateAllButton.setPadding(p, p, p, p);

            refreshButton.setBackgroundColor(Color.TRANSPARENT);
            refreshButton.setClickable(true);
            updateAllButton.setBackgroundColor(Color.TRANSPARENT);
            updateAllButton.setClickable(true);

            Drawable refreshDrawable = ContextCompat.getDrawable(context, R$d.ic_refresh_white_a60_24dp).mutate();
            refreshDrawable.setAlpha(255);
            refreshButton.setImageDrawable(refreshDrawable);
            updateAllButton.setImageDrawable(ContextCompat.getDrawable(context, R$d.ic_file_download_white_24dp));

            Runnable reRender = () -> {
                v.removeAllViews();
                onViewBound(view);
                refreshButton.setBackgroundColor(Color.TRANSPARENT);
                updateAllButton.setBackgroundColor(Color.TRANSPARENT);
            };

            refreshButton.setOnClickListener(e -> {
                setActionBarSubtitle("Updating...");
                Utils.threadPool.execute(() -> {
                    int updateCount = PluginUpdater.updateAll();
                    if (updateCount == 0) {
                        stateText = "No updates found";
                    } else if (updateCount == -1) {
                        stateText = "Something went wrong while updating. Please try again";
                    } else {
                        stateText = String.format("Successfully updated %s!", Utils.pluralise(updateCount, "plugin"));
                    }
                    Utils.mainThread.post(reRender);
                });
            });

            updateAllButton.setOnClickListener(e -> {
                setActionBarSubtitle("Checking for updates...");
                Utils.threadPool.execute(() -> {
                    PluginUpdater.checkUpdates(false);
                    int updateCount = PluginUpdater.updates.size();
                    if (updateCount == 0)
                        stateText = "No updates found";
                    else
                        stateText = String.format("Found %s", Utils.pluralise(updateCount, "update"));
                    Utils.mainThread.post(reRender);
                });
            });

            toolbar.addView(updateAllButton);
            toolbar.addView(refreshButton);
        }

        int updateCount = PluginUpdater.updates.size();

        if (updateCount == 0) {
            TextView state = new TextView(context, null, 0, R$h.UiKit_Settings_Item_SubText);
            state.setText(stateText);
            state.setPadding(padding, padding, padding, padding);
            state.setGravity(Gravity.CENTER);
            v.addView(state);
            return;
        }

        stateText = String.format("Found %s", Utils.pluralise(updateCount, "update"));
        setActionBarSubtitle(stateText);

        for (String plugin : PluginUpdater.updates)
            v.addView(new UpdaterPluginCard(context, plugin, () -> {
                v.removeAllViews();
                onViewBound(view);
            }));
    }
}
