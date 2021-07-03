/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aliucord.Constants;
import com.aliucord.Main;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.entities.Plugin.Settings.Type;
import com.aliucord.views.Button;
import com.aliucord.views.DangerButton;
import com.aliucord.views.Divider;
import com.aliucord.views.ToolbarButton;
import com.discord.utilities.color.ColorCompat;
import com.discord.views.CheckedSetting;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.google.android.material.card.MaterialCardView;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$d;
import com.lytefast.flexinput.R$h;

import java.io.File;

@SuppressLint({"SetTextI18n", "ViewConstructor"})
public final class PluginCard extends MaterialCardView {
    public final String pluginName;
    public final TextView titleView;
    public PluginCard(Context context, String name, Plugin p, FragmentManager fragmentManager, Fragment caller) {
        super(context);
        pluginName = name;
        int padding = Utils.getDefaultPadding();
        int padding2 = padding / 2;
        boolean enabled = PluginManager.isPluginEnabled(name);
        setRadius(Utils.getDefaultCardRadius());
        setCardBackgroundColor(ColorCompat.getThemedColor(context, R$b.colorBackgroundSecondary));
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        Plugin.Manifest manifest = p.getManifest();
        LinearLayout pluginLayout = new LinearLayout(context);

        Button settings = new Button(context);
        String title = String.format("%s v%s by %s", name, manifest.version, TextUtils.join(", ", manifest.authors));
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
        cs.k.b().setBackgroundColor(ColorCompat.getThemedColor(context, R$b.colorBackgroundSecondaryAlt));
        cs.setChecked(enabled);
        cs.setOnCheckedListener(e -> {
            PluginManager.togglePlugin(name);
            if (p.settings != null) settings.setEnabled(!settings.isEnabled());
        });
        pluginLayout.addView(cs);

        TextView t = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Addition);
        t.setText(manifest.description);
        t.setPadding(padding, padding, padding, padding2);
        pluginLayout.addView(new Divider(context));
        pluginLayout.addView(t);

        GridLayout buttons = new GridLayout(context);
        buttons.setRowCount(1);
        buttons.setColumnCount(4);
        buttons.setUseDefaultMargins(true);
        buttons.setPadding(padding2, 0, padding2, 0);

        if (p.settings != null) {
            settings.setText("Settings");

            if (!enabled) {
                if (Looper.myLooper() == Looper.getMainLooper()) settings.setEnabled(false);
                else Utils.mainThread.post(() -> settings.setEnabled(false));
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

        DangerButton uninstall = new DangerButton(context);
        uninstall.setText("Uninstall");
        uninstall.setOnClickListener(e -> {
            File pluginFile = new File(Constants.BASE_PATH + "/plugins/" + p.__filename + ".zip");
            if (pluginFile.exists() && !pluginFile.delete()) Main.logger.error(context, "Failed to delete plugin " + p.name, null);
            PluginManager.stopPlugin(name);
            PluginManager.plugins.remove(name);
            Main.logger.info(context, "Successfully deleted " + p.name);
            setVisibility(GONE);
        });
        buttons.addView(uninstall, new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(3)));

        AppCompatImageButton openRepo = new ToolbarButton(context);
        openRepo.setImageDrawable(ContextCompat.getDrawable(context, R$d.ic_github_white));
        openRepo.setOnClickListener(e -> {
            String url = manifest.updateUrl.replace("raw.githubusercontent.com", "github.com").replaceFirst("/builds.*", "");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            caller.startActivity(intent);
        });
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(0));
        params.setGravity(Gravity.CENTER_VERTICAL);
        buttons.addView(openRepo, params);

        pluginLayout.addView(buttons);

        addView(pluginLayout);
    }

    @Override
    public String toString() {
        return "PluginCard, " + titleView.getText();
    }
}
