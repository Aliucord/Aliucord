/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.*;
import android.widget.TextView;
import android.widget.GridLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.aliucord.*;
import com.aliucord.entities.Plugin;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.views.*;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.changelog.WidgetChangeLog;
import com.google.android.material.card.MaterialCardView;
import com.lytefast.flexinput.*;

@SuppressLint({"SetTextI18n", "ViewConstructor"})
public class UpdaterPluginCard extends MaterialCardView {
    public UpdaterPluginCard(Context context, String plugin, Runnable forceUpdate) {
        super(context);
        int padding = Utils.getDefaultPadding();
        int paddingHalf = padding / 2;

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, paddingHalf, 0, 0);
        setLayoutParams(params);
        setUseCompatPadding(true);
        setCardBackgroundColor(ColorCompat.getThemedColor(context, R$b.colorBackgroundSecondary));
        setRadius(Utils.getDefaultCardRadius());
        setContentPadding(padding, padding, padding, padding);

        Plugin p = PluginManager.plugins.get(plugin);
        assert p != null;

        ConstraintLayout layout = new ConstraintLayout(context);
        TextView tv = new TextView(context, null, 0, R$h.UiKit_TextView_H2);
        tv.setText(plugin);
        int id = View.generateViewId();
        tv.setId(id);
        layout.addView(tv);

        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainedHeight(id, true);
        set.connect(id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
        set.connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.applyTo(layout);

        GridLayout buttonLayout = new GridLayout(context);
        buttonLayout.setRowCount(1);
        buttonLayout.setColumnCount(2);
        buttonLayout.setUseDefaultMargins(true);
        buttonLayout.setPadding(0, 0, 0, 0);
        int btnLayoutId = View.generateViewId();
        buttonLayout.setId(btnLayoutId);

        tv = new TextView(context, null, 0, R$h.UiKit_TextView_Subtext);
        try {
            PluginUpdater.UpdateInfo info = PluginUpdater.getUpdateInfo(p);
            tv.setText(String.format("%s -> v%s", p.getManifest().version, info != null ? info.version : "?"));
            if (info != null && info.changelog != null) {
                ToolbarButton changeLogButton = new ToolbarButton(context);
                changeLogButton.setImageDrawable(ContextCompat.getDrawable(context, R$d.ic_history_white_24dp));
                changeLogButton.setPadding(paddingHalf, paddingHalf, paddingHalf, paddingHalf);
                changeLogButton.setOnClickListener(e ->
                    WidgetChangeLog.Companion.launch(context, p.name + " v" + info.version, "1", info.changelogMedia != null ? info.changelogMedia : "https://cdn.discordapp.com/banners/169256939211980800/eda024c8f40a45c88265a176f0926bea.jpg?size=2048", info.changelog));

                GridLayout.LayoutParams clParams = new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(0));
                clParams.setGravity(Gravity.CENTER_VERTICAL);
                buttonLayout.addView(changeLogButton, clParams);
            }
        } catch (Throwable e) { PluginManager.logger.error(e); }
        int verid = View.generateViewId();
        tv.setId(verid);
        layout.addView(tv);

        set = new ConstraintSet();
        set.clone(layout);
        set.constrainedHeight(verid, true);
        set.connect(verid, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
        set.connect(verid, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.applyTo(layout);

        ToolbarButton update = new ToolbarButton(context);
        update.setImageDrawable(ContextCompat.getDrawable(context, R$d.ic_file_download_white_24dp));
        update.setPadding(paddingHalf, paddingHalf, 0, paddingHalf);
        update.setOnClickListener(e -> {
            update.setEnabled(false);
            Utils.threadPool.execute(() -> {
                try {
                    PluginUpdater.update(plugin);
                    PluginUpdater.updates.remove(plugin);
                    PluginManager.logger.info(context, "Successfully updated " + p.name);
                } catch (Throwable t) {
                    PluginManager.logger.error(context, "Sorry, something went wrong while updating " + p.name, t);
                } finally {
                    Utils.mainThread.post(forceUpdate);
                }
            });
        });

        GridLayout.LayoutParams updateParams = new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(1));
        updateParams.setGravity(Gravity.CENTER_VERTICAL);
        buttonLayout.addView(update, updateParams);
        layout.addView(buttonLayout);
        
        set = new ConstraintSet();
        set.clone(layout);
        set.connect(btnLayoutId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
        set.connect(btnLayoutId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.applyTo(layout);
        addView(layout);
    }
}
