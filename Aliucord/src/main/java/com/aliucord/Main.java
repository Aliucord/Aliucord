/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.settings.Plugins;
import com.aliucord.settings.Updater;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.views.Divider;
import com.discord.app.AppActivity;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.guilds.invite.WidgetGuildInvite;
import com.discord.widgets.settings.WidgetSettings;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$d;
import com.lytefast.flexinput.R$h;

import java.io.File;
import java.util.*;

@SuppressWarnings({"unchecked"})
public class Main {
    public static boolean preInitialized = false;
    public static boolean initialized = false;
    public static final Logger logger = new Logger();

    @Deprecated
    @SuppressWarnings("deprecation")
    public static Map<String, List<String>> getClassesToPatch() {
        return getClassesToPatch(true);
    }

    @Deprecated
    public static Map<String, List<String>> getClassesToPatch(boolean loadPlugins) {
        return Collections.emptyMap();
    }

    public static void preInit(AppActivity activity) {
        if (preInitialized) return;
        preInitialized = true;

        Utils.appActivity = activity;
        PluginManager.loadCorePlugins(activity);

        if (checkPermissions(activity)) {
            File dir = new File(Constants.BASE_PATH + "/plugins");
            if (!dir.exists()) {
                boolean res = dir.mkdirs();
                if (!res) logger.error("Failed to create directories!", null);
            }
            for (File f : Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(".zip"))))
                PluginManager.loadPlugin(activity, f);
        }
    }

    @SuppressLint("SetTextI18n")
    public static void init(AppActivity activity) {
        if (initialized) return;
        initialized = true;

        Patcher.addPatch(WidgetSettings.class, "onViewBound", new Class<?>[]{ View.class }, new PinePatchFn(callFrame -> {
            Context context = ((WidgetSettings) callFrame.thisObject).requireContext();
            CoordinatorLayout view = (CoordinatorLayout) callFrame.args[0];
            LinearLayoutCompat v = (LinearLayoutCompat) ((NestedScrollView)
                view.getChildAt(1)).getChildAt(0);

            int baseIndex = v.indexOfChild(v.findViewById(Utils.getResId("developer_options_divider", "id")));
            v.addView(new Divider(context), baseIndex);
            TextView header = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Header);
            header.setText("Aliucord");
            header.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
            v.addView(header, baseIndex + 1);

            Typeface font = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium);
            TextView plugins = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Icon);
            plugins.setText("Plugins");
            plugins.setTypeface(font);
            int iconColor = ColorCompat.getThemedColor(context, R$b.colorInteractiveNormal);
            Drawable icon = ContextCompat.getDrawable(context, R$d.ic_clear_all_white_24dp);
            if (icon != null) {
                Drawable copy = icon.mutate();
                copy.setTint(iconColor);
                plugins.setCompoundDrawablesRelativeWithIntrinsicBounds(copy, null, null, null);
            }
            plugins.setOnClickListener(e -> Utils.openPage(e.getContext(), Plugins.class));
            v.addView(plugins, baseIndex + 2);

            TextView updater = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Icon);
            updater.setText("Updater");
            updater.setTypeface(font);
            icon = ContextCompat.getDrawable(context, R$d.ic_file_download_white_24dp);
            if (icon != null) {
                Drawable copy = icon.mutate();
                copy.setTint(iconColor);
                updater.setCompoundDrawablesRelativeWithIntrinsicBounds(copy, null, null, null);
            }
            updater.setOnClickListener(e -> Utils.openPage(e.getContext(), Updater.class));
            v.addView(updater, baseIndex + 3);

            TextView version = v.findViewById(Utils.getResId("app_info_header", "id"));
            version.setText(version.getText() + " | Aliucord " + BuildConfig.GIT_REVISION);

            TextView uploadLogs = v.findViewById(Utils.getResId("upload_debug_logs", "id"));
            uploadLogs.setText("Aliucord Support Server");
            uploadLogs.setOnClickListener(e -> WidgetGuildInvite.Companion.launch(e.getContext(), Constants.ALIUCORD_SUPPORT, ""));
        }));

        // Patch to repair built-in emotes is needed because installer doesn't recompile resources,
        // so they stay in package com.discord instead of apk package name
        Patcher.addPatch("com.discord.models.domain.emoji.ModelEmojiUnicode", "getImageUri", new Class<?>[]{ String.class, Context.class },
            new PinePatchFn(callFrame -> {
                String name = "emoji_" + callFrame.args[0];
                callFrame.setResult("res:///" + Utils.getResId(name, "raw"));
            })
        );

        PluginManager.startCorePlugins(activity);

        for (String name : PluginManager.plugins.keySet()) {
            try {
                if (!PluginManager.isPluginEnabled(name)) continue;
                PluginManager.startPlugin(name);
            } catch (Throwable e) { PluginManager.logger.error("Exception while starting plugin: " + name, e); }
        }

        Utils.threadPool.execute(() -> PluginUpdater.checkUpdates(true));
    }

    private static boolean checkPermissions(AppCompatActivity activity) {
        String perm = "android.permission.WRITE_EXTERNAL_STORAGE";
        if (activity.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) return true;
        activity.requestPermissions(new String[]{ perm }, 1);
        return false;
    }
}
