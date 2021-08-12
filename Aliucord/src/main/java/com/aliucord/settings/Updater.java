/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.aliucord.SettingsUtils;
import com.aliucord.Utils;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.views.ToolbarButton;
import com.aliucord.widgets.BottomSheet;
import com.aliucord.widgets.UpdaterPluginCard;
import com.discord.views.CheckedSetting;
import com.google.android.material.snackbar.Snackbar;
import com.lytefast.flexinput.R$d;
import com.lytefast.flexinput.R$h;

public class Updater extends SettingsPage {
    public static class UpdaterSettings extends BottomSheet {
        public static final String AUTO_UPDATE_KEY = "AC_auto_update_enabled";

        @Override
        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);

            boolean autoUpdateEnabled = SettingsUtils.getBool(AUTO_UPDATE_KEY, false);
            CheckedSetting autoUpdateSwitch = Utils.createCheckedSetting(requireContext(), CheckedSetting.ViewType.SWITCH, "Auto update", "Whether plugins should automatically be updated");
            autoUpdateSwitch.setChecked(autoUpdateEnabled);
            autoUpdateSwitch.setOnCheckedListener(c -> SettingsUtils.setBool(AUTO_UPDATE_KEY, c));

            addView(autoUpdateSwitch);
        }
    }

    private static final int id = View.generateViewId();
    private String stateText = "No new updates found";

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SetTextI18n")
    public void onViewBound(View view) {
        super.onViewBound(view);

        setActionBarTitle("Updater");
        setActionBarSubtitle(stateText);

        Context context = requireContext();
        int padding = Utils.getDefaultPadding();

        Utils.threadPool.execute(() -> {
                Snackbar sb;
                if (!com.aliucord.updater.Updater.isAliucordOfficial()) {
                    sb = Snackbar.make(getLinearLayout(), "You're using an unofficial Aliucord build. Please do not report bugs.", Snackbar.LENGTH_INDEFINITE);
                } else if (com.aliucord.updater.Updater.isAliucordOutdated()) {
                    sb = Snackbar
                            .make(getLinearLayout(), "Your Aliucord is outdated. Please update it via the Aliucord Installer.", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Update", v -> {
                                Context ctx = v.getContext();
                                Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.aliucord.installer");
                                if (i != null)
                                    ctx.startActivity(i);
                                else
                                    Utils.showToast(ctx, "Please install the Aliucord Installer and try again.");
                            });
                } else return;

                // https://developer.android.com/reference/android/R.color#holo_orange_light
                sb.setBackgroundTint(0xffffbb33).setTextColor(Color.BLACK).show();
        });

        if (getHeaderBar().findViewById(id) == null) {
            int p = padding / 2;

            ToolbarButton refreshButton = new ToolbarButton(context);
            refreshButton.setId(id);
            ToolbarButton updateAllButton = new ToolbarButton(context);
            ToolbarButton settingsButton = new ToolbarButton(context);

            Toolbar.LayoutParams childParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
            childParams.gravity = Gravity.END;
            refreshButton.setLayoutParams(childParams);
            updateAllButton.setLayoutParams(childParams);

            Toolbar.LayoutParams marginEndParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
            marginEndParams.gravity = Gravity.END;
            marginEndParams.setMarginEnd(p);
            settingsButton.setLayoutParams(marginEndParams);
            refreshButton.setPadding(p, p, p, p);
            settingsButton.setPadding(p, p, p, p);
            updateAllButton.setPadding(p, p, p, p);

            //noinspection ConstantConditions
            refreshButton.setImageDrawable(Utils.tintToTheme(ContextCompat.getDrawable(context, R$d.ic_refresh_white_a60_24dp).mutate()), false);
            updateAllButton.setImageDrawable(ContextCompat.getDrawable(context, R$d.ic_file_download_white_24dp));
            settingsButton.setImageDrawable(ContextCompat.getDrawable(context, R$d.ic_guild_settings_24dp));

            updateAllButton.setOnClickListener(e -> {
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
            });

            refreshButton.setOnClickListener(e -> {
                setActionBarSubtitle("Checking for updates...");
                Utils.threadPool.execute(() -> {
                    PluginUpdater.checkUpdates(false);
                    int updateCount = PluginUpdater.updates.size();
                    if (updateCount == 0)
                        stateText = "No updates found";
                    else
                        stateText = String.format("Found %s", Utils.pluralise(updateCount, "update"));
                    Utils.mainThread.post(this::reRender);
                });
            });

            settingsButton.setOnClickListener(e -> new UpdaterSettings().show(getParentFragmentManager(), "Updater Settings"));

            addHeaderButton(settingsButton);
            addHeaderButton(updateAllButton);
            addHeaderButton(refreshButton);
        }

        int updateCount = PluginUpdater.updates.size();

        if (updateCount == 0) {
            TextView state = new TextView(context, null, 0, R$h.UiKit_Settings_Item_SubText);
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
