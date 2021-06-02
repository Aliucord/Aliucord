/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;

import com.aliucord.Constants;
import com.aliucord.Main;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.entities.Plugin.Settings.Type;
import com.aliucord.views.Button;
import com.aliucord.views.Divider;
import com.discord.utilities.color.ColorCompat;
import com.discord.views.CheckedSetting;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.google.android.material.card.MaterialCardView;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$h;

import java.io.File;

@SuppressLint({"SetTextI18n", "ViewConstructor"})
public final class PluginCard extends MaterialCardView {
    public final String pluginName;
    public final TextView titleView;
    public PluginCard(Context context, String name, Plugin p, FragmentManager fragmentManager) {
        super(context);
        pluginName = name;
        int padding = Utils.getDefaultPadding();
        int padding2 = padding / 2;
        boolean enabled = PluginManager.isPluginEnabled(name);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, padding2, 0, 0);
        setLayoutParams(params);
        setUseCompatPadding(true);
        setCardBackgroundColor(ColorCompat.getThemedColor(context, R$b.primary_630));
        setStrokeColor(ColorCompat.getThemedColor(context, R$b.primary_900));
        setStrokeWidth(Utils.dpToPx(0.5f));
        setRadius(Utils.dpToPx(4));

        Plugin.Manifest manifest = p.getManifest();
        LinearLayout pluginLayout = new LinearLayout(context);

        Button settings = new Button(context, false);
        String title = name + " v" + manifest.version + " by " + TextUtils.join(", ", manifest.authors);
        SpannableString spannableTitle = new SpannableString(title);
        for (Plugin.Manifest.Author author : manifest.authors) {
            if (author.id < 1) continue;
            int i = title.indexOf(author.name, name.length() + 2 + manifest.version.length() + 3);
            spannableTitle.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    WidgetUserSheet.Companion.show(author.id, fragmentManager);
                }
            }, i, i + author.name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        CheckedSetting cs = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH,
                spannableTitle, null);
        titleView = cs.k.a();
        titleView.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
        titleView.setMovementMethod(LinkMovementMethod.getInstance());
        cs.setChecked(enabled);
        cs.setOnCheckedListener(e -> {
            PluginManager.togglePlugin(name);
            if (p.settings != null) settings.setEnabled(!settings.isEnabled());
        });
        pluginLayout.addView(cs);
        pluginLayout.addView(new Divider(context));

        TextView t = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Addition);
        t.setText(manifest.description);
        t.setPadding(padding, padding2, padding, padding2);
        pluginLayout.addView(t);
        pluginLayout.addView(new Divider(context));

        GridLayout buttons = new GridLayout(context);
        buttons.setRowCount(1);
        buttons.setColumnCount(4);
        buttons.setUseDefaultMargins(true);
        buttons.setPadding(0, 0, padding2, 0);

        if (p.settings != null) {
            settings.setText("Settings");

            if (!enabled) {
                if (Looper.myLooper() == Looper.getMainLooper()) settings.setEnabled(false);
                else new Handler(Looper.getMainLooper()).post(() -> settings.setEnabled(false));
            }
            if (p.settings.type == Type.PAGE && p.settings.page != null)
                settings.setOnClickListener(v -> {
                    try {
                        Utils.openPageWithProxy(v.getContext(), p.settings.page.newInstance());
                    } catch (Throwable e) { Main.logger.error(e); }
                });
            else if (p.settings.type == Type.BOTTOMSHEET && p.settings.bottomSheet != null)
                settings.setOnClickListener(v -> {
                    try {
                        p.settings.bottomSheet.newInstance().show(fragmentManager, name + "Settings");
                    } catch (Throwable e) { Main.logger.error(e); }
                });

            buttons.addView(settings, new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(2)));
        }

        Button uninstall = new Button(context, true);
        uninstall.setText("Uninstall");
        uninstall.setOnClickListener(e -> {
            File pluginFile = new File(Constants.BASE_PATH + "/plugins/" + p.__filename + ".zip");
            if (pluginFile.exists() && !pluginFile.delete()) Main.logger.error("Failed to delete plugin", null);
            PluginManager.stopPlugin(name);
            PluginManager.plugins.remove(name);
            setVisibility(GONE);
        });
        buttons.addView(uninstall, new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(3)));

        pluginLayout.addView(buttons);

        addView(pluginLayout);
    }

    @Override
    public String toString() {
        return "PluginCard, " + titleView.getText();
    }
}
