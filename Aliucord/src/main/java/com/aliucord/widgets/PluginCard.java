/*
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
import com.aliucord.Utils;
import com.aliucord.views.*;
import com.discord.utilities.color.ColorCompat;
import com.discord.views.CheckedSetting;
import com.google.android.material.card.MaterialCardView;
import com.lytefast.flexinput.*;

public class PluginCard extends MaterialCardView {
    public final LinearLayout root;
    public final CheckedSetting switchHeader;
    public final TextView titleView;
    public final TextView descriptionView;
    public final GridLayout buttonLayout;
    public final Button settingsButton;
    public final DangerButton uninstallButton;
    public final ToolbarButton repoButton;

    @SuppressLint("SetTextI18n")
    public PluginCard(Context ctx) {
        super(ctx);
        setRadius(Utils.getDefaultCardRadius());
        setCardBackgroundColor(ColorCompat.getThemedColor(ctx, R$b.colorBackgroundSecondary));
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        int p = Utils.getDefaultPadding();
        int p2 = p / 2;

        root = new LinearLayout(ctx);
        switchHeader = new CheckedSetting(ctx, null);
        switchHeader.removeAllViews();
        switchHeader.f(CheckedSetting.ViewType.SWITCH);

        View headerRoot = switchHeader.k.b();
        headerRoot.setPadding(0, headerRoot.getPaddingTop(), headerRoot.getPaddingRight(), headerRoot.getPaddingBottom());
        headerRoot.setBackgroundColor(ColorCompat.getThemedColor(ctx, R$b.colorBackgroundSecondaryAlt));

        switchHeader.setSubtext(null);
        titleView = switchHeader.k.a();
        titleView.setTextSize(16.0f);
        titleView.setTypeface(ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_semibold));
        titleView.setMovementMethod(LinkMovementMethod.getInstance());

        root.addView(switchHeader);
        root.addView(new Divider(ctx));

        descriptionView = new TextView(ctx, null, 0, R$h.UiKit_Settings_Item_Addition);
        descriptionView.setPadding(p, p, p, p2);
        root.addView(descriptionView);

        buttonLayout = new GridLayout(ctx);
        buttonLayout.setRowCount(1);
        buttonLayout.setColumnCount(4);
        buttonLayout.setUseDefaultMargins(true);
        buttonLayout.setPadding(p2, 0, p2, 0);

        settingsButton = new Button(ctx);
        settingsButton.setText("Settings");

        uninstallButton = new DangerButton(ctx);
        uninstallButton.setText("Uninstall");

        repoButton = new ToolbarButton(ctx);
        repoButton.setImageDrawable(ContextCompat.getDrawable(ctx, R$d.ic_github_white));

        buttonLayout.addView(settingsButton, new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(2)));
        buttonLayout.addView(uninstallButton, new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(3)));

        GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(0));
        params.setGravity(Gravity.CENTER_VERTICAL);
        buttonLayout.addView(repoButton, params);

        root.addView(buttonLayout);

        addView(root);
    }
}
