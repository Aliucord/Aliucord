/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings;

import static com.aliucord.updater.Updater.isAliucordOutdated;
import static com.aliucord.updater.Updater.isDiscordOutdated;
import static com.aliucord.updater.Updater.updateAliucord;
import static com.aliucord.updater.Updater.usingDexFromStorage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.aliucord.SettingsUtils;
import com.aliucord.Utils;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.utils.DimenUtils;
import com.aliucord.widgets.BottomSheet;
import com.aliucord.widgets.UpdaterPluginCard;
import com.discord.views.CheckedSetting;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.lytefast.flexinput.R;

public class Updater extends SettingsPage {
    public static class UpdaterSettings extends BottomSheet {
        public static final String AUTO_UPDATE_PLUGINS_KEY = "AC_plugins_auto_update_enabled";
        public static final String AUTO_UPDATE_ALIUCORD_KEY = "AC_aliucord_auto_update_enabled";
        public static final String ALIUCORD_FROM_STORAGE = "AC_from_storage";

        @Override
        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);

            var ctx = view.getContext();

            addView(createSwitch(ctx, "Auto Update Aliucord", "Whether Aliucord should automatically be updated", AUTO_UPDATE_ALIUCORD_KEY));
            addView(createSwitch(ctx, "Auto Update Plugins", "Whether Plugins should automatically be updated", AUTO_UPDATE_PLUGINS_KEY));

            var dexFromStorageSwitch = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "Aliucord from storage", "Use custom Aliucord build from Aliucord/Aliucord.zip");
            dexFromStorageSwitch.setChecked(SettingsUtils.getBool(ALIUCORD_FROM_STORAGE, false));
            dexFromStorageSwitch.setOnCheckedListener(c -> {
                if (!c) SettingsUtils.setBool(ALIUCORD_FROM_STORAGE, false);
                else {
                    // Spooky, lets make sure no one gets scammed
                    var dialog = new ConfirmDialog();
                    dialog
                        .setIsDangerous(true)
                        .setTitle("HOLD ON")
                        .setDescription("If someone else told you to do this, you are LIKELY GETTING SCAMMED. Only check this option if you know what you're doing!")
                        .setOnOkListener(v -> {
                            SettingsUtils.setBool(ALIUCORD_FROM_STORAGE, true);
                            dialog.dismiss();
                        })
                        .setOnCancelListener(v -> {
                            dexFromStorageSwitch.setChecked(false);
                            dialog.dismiss();
                        })
                        .show(getParentFragmentManager(), "ALIUCORD_FROM_STORAGE_WARNING");
                }
            });

            addView(dexFromStorageSwitch);
        }

        private CheckedSetting createSwitch(Context ctx, String text, String subText, String settingsKey) {
            var cs = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, text, subText);
            cs.setChecked(SettingsUtils.getBool(settingsKey, false));
            cs.setOnCheckedListener(c -> SettingsUtils.setBool(settingsKey, c));
            return cs;
        }
    }

    private String stateText = "No new updates found";

    @Override
    @SuppressLint("SetTextI18n")
    public void onViewBound(View view) {
        super.onViewBound(view);

        setActionBarTitle("Updater");
        setActionBarSubtitle(stateText);

        var context = view.getContext();
        int padding = DimenUtils.getDefaultPadding();

        Utils.threadPool.execute(() -> {
            Snackbar sb;
            if (usingDexFromStorage()) {
                sb = Snackbar.make(getLinearLayout(), "Updater disabled due to using Aliucord from storage.", Snackbar.LENGTH_INDEFINITE);
            } else if (isDiscordOutdated()) {
                sb = Snackbar
                    .make(getLinearLayout(), "Your Base Discord is outdated. Please update using the installer.", BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setAction("Open Installer", v -> {
                        var ctx = v.getContext();
                        var i = ctx.getPackageManager().getLaunchIntentForPackage("com.aliucord.installer");
                        if (i != null)
                            ctx.startActivity(i);
                        else
                            Utils.showToast("Please install the Aliucord installer and try again.");
                    });
            } else if (isAliucordOutdated()) {
                sb = Snackbar
                    .make(getLinearLayout(), "Your Aliucord is outdated.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Update", v -> Utils.threadPool.execute(() -> {
                        var ctx = v.getContext();
                        try {
                            updateAliucord(ctx);
                            Utils.showToast("Successfully updated Aliucord.");
                            Snackbar rb = Snackbar
                                .make(getLinearLayout(), "Restart to apply the update.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Restart", e -> {
                                    Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                                    context.startActivity(Intent.makeRestartActivityTask(intent.getComponent()));
                                    Runtime.getRuntime().exit(0);
                                });
                            rb.setBackgroundTint(0xffffbb33);
                            rb.setTextColor(Color.BLACK);
                            rb.setActionTextColor(Color.BLACK);
                            rb.show();
                        } catch (Throwable th) {
                            PluginUpdater.logger.errorToast("Failed to update Aliucord. Check the debug log for more info", th);
                        }
                    }));
            } else return;

            sb
                .setBackgroundTint(0xffffbb33) // https://developer.android.com/reference/android/R.color#holo_orange_light
                .setTextColor(Color.BLACK)
                .setActionTextColor(Color.BLACK)
                .show();
        });

        addHeaderButton("Refresh", R.e.ic_refresh_white_a60_24dp, item -> {
            item.setEnabled(false);
            setActionBarSubtitle("Checking for updates...");
            Utils.threadPool.execute(() -> {
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
        addHeaderButton("Settings", R.e.ic_guild_settings_24dp, item -> {
            new UpdaterSettings().show(getParentFragmentManager(), "Updater Settings");
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
