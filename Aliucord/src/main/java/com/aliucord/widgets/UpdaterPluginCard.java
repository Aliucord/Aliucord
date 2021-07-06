/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.aliucord.Main;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.views.Button;
import com.discord.utilities.color.ColorCompat;
import com.google.android.material.card.MaterialCardView;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$h;

@SuppressLint({"SetTextI18n", "ViewConstructor"})
public class UpdaterPluginCard extends MaterialCardView {
    public UpdaterPluginCard(Context context, String plugin, Runnable forceUpdate) {
        super(context);
        int padding = Utils.getDefaultPadding();

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, padding / 2, 0, 0);
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

        tv = new TextView(context, null, 0, R$h.UiKit_TextView_Subtext);
        try {
            PluginUpdater.UpdateInfo info = PluginUpdater.getUpdateInfo(p);
            tv.setText(String.format("%s -> v%s", p.getManifest().version, info != null ? info.version : "?"));
        } catch (Throwable e) { Main.logger.error(e); }
        int verid = View.generateViewId();
        tv.setId(verid);
        layout.addView(tv);

        set = new ConstraintSet();
        set.clone(layout);
        set.constrainedHeight(verid, true);
        set.connect(verid, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
        set.connect(verid, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.applyTo(layout);

        Button update = new Button(context);
        update.setText("Update");
        update.setOnClickListener(e -> {
            update.setEnabled(false);
            update.setText("Updating..");
            Utils.threadPool.execute(() -> {
                try {
                    PluginUpdater.update(plugin);
                    PluginUpdater.updates.remove(plugin);
                    Main.logger.info(context, "Successfully updated " + p.name);
                } catch (Throwable t) {
                    Main.logger.error(context, "Sorry, something went wrong while updating " + p.name, t);
                } finally {
                    Utils.mainThread.post(forceUpdate);
                }
            });
        });
        int updateId = View.generateViewId();
        update.setId(updateId);
        layout.addView(update);

        set = new ConstraintSet();
        set.clone(layout);
        set.connect(updateId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
        set.connect(updateId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.applyTo(layout);
        addView(layout);
    }
}
