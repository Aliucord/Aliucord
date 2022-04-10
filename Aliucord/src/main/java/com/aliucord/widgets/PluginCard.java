/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.aliucord.Constants;
import com.aliucord.utils.DimenUtils;
import com.aliucord.views.*;
import com.discord.utilities.color.ColorCompat;
import com.discord.views.CheckedSetting;
import com.google.android.material.card.MaterialCardView;
import com.lytefast.flexinput.R;

public class PluginCard extends MaterialCardView {
    public final LinearLayout root;
    public final CheckedSetting switchHeader;
    public final TextView titleView;
    public final TextView descriptionView;
    public final GridLayout buttonLayout;
    public final Button settingsButton;
    public final DangerButton uninstallButton;
    public final ToolbarButton repoButton;
    public final ToolbarButton changeLogButton;

    @SuppressLint("SetTextI18n")
    public PluginCard(Context ctx) {
        super(ctx);
        setRadius(DimenUtils.getDefaultCardRadius());
        setCardBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundSecondary));
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        int p = DimenUtils.getDefaultPadding();
        int p2 = p / 2;

        root = new LinearLayout(ctx);
        switchHeader = new CheckedSetting(ctx, null);
        switchHeader.removeAllViews();
        switchHeader.f(CheckedSetting.ViewType.SWITCH);

        View headerRoot = switchHeader.l.b();
        headerRoot.setPadding(0, headerRoot.getPaddingTop(), headerRoot.getPaddingRight(), headerRoot.getPaddingBottom());
        headerRoot.setBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundSecondaryAlt));

        switchHeader.setSubtext(null);
        titleView = switchHeader.l.a();
        titleView.setTextSize(16.0f);
        titleView.setTypeface(ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_semibold));
        titleView.setMovementMethod(LinkMovementMethod.getInstance());

        root.addView(switchHeader);
        root.addView(new Divider(ctx));

        descriptionView = new TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Addition);
        descriptionView.setPadding(p, p, p, p2);
        root.addView(descriptionView);

        buttonLayout = new GridLayout(ctx);
        buttonLayout.setRowCount(1);
        buttonLayout.setColumnCount(5);
        buttonLayout.setUseDefaultMargins(true);
        buttonLayout.setPadding(p2, 0, p2, 0);

        settingsButton = new Button(ctx);
        settingsButton.setText(context.getString(R.h.settings));

        uninstallButton = new DangerButton(ctx);
        uninstallButton.setText(ctx.getString(R.h.delete));

        repoButton = new ToolbarButton(ctx);
        repoButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.e.ic_account_github_white_24dp));

        changeLogButton = new ToolbarButton(ctx);
        changeLogButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.e.ic_history_white_24dp));

        buttonLayout.addView(settingsButton, new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(3)));
        buttonLayout.addView(uninstallButton, new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(4)));

        GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(0));
        params.setGravity(Gravity.CENTER_VERTICAL);
        buttonLayout.addView(repoButton, params);
        GridLayout.LayoutParams clparams = new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(1));
        clparams.setGravity(Gravity.CENTER_VERTICAL);
        buttonLayout.addView(changeLogButton, clparams);

        root.addView(buttonLayout);

        addView(root);
    }
}
