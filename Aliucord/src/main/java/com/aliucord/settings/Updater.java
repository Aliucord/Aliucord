/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.updater.CoreUpdater;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.utils.DimenUtils;
import com.aliucord.widgets.UpdaterPluginCard;
import com.google.android.material.snackbar.Snackbar;
import com.lytefast.flexinput.R;

public class Updater extends SettingsPage {
    private String stateText = "No new updates found";

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);

        setActionBarTitle("Updater");
        setActionBarSubtitle(stateText);

        var context = view.getContext();
        int padding = DimenUtils.getDefaultPadding();

        Utils.threadPool.execute(() -> {
            Snackbar sb;
            if (CoreUpdater.isCustomCoreLoaded()) {
                sb = Snackbar.make(getLinearLayout(), "Core updates are currently disabled due to using Aliucord from storage.", Snackbar.LENGTH_INDEFINITE);
            } else if (CoreUpdater.isUpdaterDisabled()) {
                sb = Snackbar.make(getLinearLayout(), "All updates have been manually disabled.", Snackbar.LENGTH_INDEFINITE);
            } else {
                return;
            }

            sb
                .setBackgroundTint(context.getColor(android.R.color.holo_orange_light))
                .setTextColor(Color.BLACK)
                .setActionTextColor(Color.BLACK)
                .show();
        });

        addHeaderButton("Refresh", R.e.ic_refresh_white_a60_24dp, item -> {
            item.setEnabled(false);
            setActionBarSubtitle("Checking for updates...");
            Utils.threadPool.execute(() -> {
                // FIXME: notifications aren't shown on updater screen
                CoreUpdater.checkForUpdates();
                PluginUpdater.cache.clear();
                PluginUpdater.checkUpdates(false);
                int updateCount = PluginUpdater.updates.size();
                if (updateCount == 0)
                    stateText = "No updates found";
                else
                    stateText = String.format("Found %s", Utils.pluralise(updateCount, "update"));
                Utils.mainThread.post(this::reRender);
            });
            return true;
        });

        addHeaderButton("Update All", R.e.ic_file_download_white_24dp, item -> {
            item.setEnabled(false);
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
                Utils.mainThread.post(this::reRender);
            });
            return true;
        });

        int updateCount = PluginUpdater.updates.size();

        if (updateCount == 0) {
            TextView state = new TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText);
            state.setText(stateText);
            state.setPadding(padding, padding, padding, padding);
            state.setGravity(Gravity.CENTER);
            addView(state);
            return;
        }

        stateText = "Found " + Utils.pluralise(updateCount, "update");
        setActionBarSubtitle(stateText);

        for (String plugin : PluginUpdater.updates)
            addView(new UpdaterPluginCard(context, plugin, this::reRender));
    }
}
