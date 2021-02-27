package com.aliucord.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Main;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.views.Button;
import com.aliucord.views.Divider;
import com.aliucord.widgets.UpdaterPluginCard;
import com.lytefast.flexinput.R$h;

public class Updater extends SettingsPage {
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setActionBarTitle("Updater");
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onViewBound(View view) {
        super.onViewBound(view);

        Context context = requireContext();
        int padding = Utils.getDefaultPadding();
        LinearLayout v = (LinearLayout) ((NestedScrollView) ((CoordinatorLayout)
                view).getChildAt(1)).getChildAt(0);
        v.setPadding(padding, padding, padding, padding);

        TextView state = new TextView(context, null, 0, R$h.UiKit_Settings_Item_SubText);
        state.setText("No new updates found");
        state.setPadding(0, padding / 2, 0, padding / 2);

        LinearLayout buttons = new LinearLayout(context);

        Button btn = new Button(context, false);
        btn.setText("Check for Updates");
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, padding, 0);
        btn.setLayoutParams(layoutParams);
        btn.setOnClickListener(e -> {
            state.setText("Cheecking for updates...");
            new Thread(() -> {
                PluginUpdater.checkUpdates(false);
                new Handler(Looper.getMainLooper()).post(() -> {
                    v.removeAllViews();
                    onViewBound(view);
                });
            }).start();
        });
        buttons.addView(btn);

        btn = new Button(context, false);
        btn.setText("Update All");
        btn.setOnClickListener(e -> {
            state.setText("Updating...");
            new Thread(() -> {
                PluginUpdater.updateAll();
                new Handler(Looper.getMainLooper()).post(() -> {
                    v.removeAllViews();
                    onViewBound(view);
                });
            }).start();
        });
        buttons.addView(btn);
        v.addView(buttons);
        v.addView(state);

        if (PluginUpdater.updates.size() == 0) return;
        state.setText("New updates are available");
        v.addView(new Divider(context));

        Runnable forceUpdate = () -> {
            v.removeAllViews();
            onViewBound(view);
        };
        for (String plugin : PluginUpdater.updates) v.addView(new UpdaterPluginCard(context, plugin, forceUpdate));
    }
}
