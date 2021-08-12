/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.*;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.coreplugins.CorePlugins;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.settings.*;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.views.Divider;
import com.discord.app.AppActivity;
import com.discord.app.AppLog;
import com.discord.databinding.WidgetDebuggingAdapterItemBinding;
import com.discord.models.domain.emoji.ModelEmojiUnicode;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.debugging.WidgetDebugging;
import com.discord.widgets.guilds.invite.WidgetGuildInvite;
import com.discord.widgets.settings.WidgetSettings;
import com.lytefast.flexinput.*;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public final class Main {
    /** Whether Aliucord has been preInitialized */
    public static boolean preInitialized = false;
    /** Whether Aliucord has been initialized */
    public static boolean initialized = false;
    public static final Logger logger = new Logger();

    private static boolean loadedPlugins;

    /** Aliucord's preInit hook. Plugins are loaded here */
    public static void preInit(AppActivity activity) {
        if (preInitialized) return;
        preInitialized = true;

        Utils.appActivity = activity;
        CorePlugins.loadAll(activity);

        if (checkPermissions(activity)) loadAllPlugins(activity);
    }

    /** Aliucord's init hook. Plugins are started here */
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

            TextView crashes = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Icon);
            crashes.setText("Crashes");
            crashes.setTypeface(font);
            icon = ContextCompat.getDrawable(context, R$d.ic_history_white_24dp);
            if (icon != null) {
                Drawable copy = icon.mutate();
                copy.setTint(iconColor);
                crashes.setCompoundDrawablesRelativeWithIntrinsicBounds(copy, null, null, null);
            }
            crashes.setOnClickListener(e -> Utils.openPage(e.getContext(), Crashes.class));
            v.addView(crashes, baseIndex + 4);

            TextView version = v.findViewById(Utils.getResId("app_info_header", "id"));
            version.setText(version.getText() + " | Aliucord " + BuildConfig.GIT_REVISION);

            TextView uploadLogs = v.findViewById(Utils.getResId("upload_debug_logs", "id"));
            uploadLogs.setText("Aliucord Support Server");
            uploadLogs.setOnClickListener(e -> WidgetGuildInvite.Companion.launch(e.getContext(), Constants.ALIUCORD_SUPPORT, ""));
        }));

        // Patch to repair built-in emotes is needed because installer doesn't recompile resources,
        // so they stay in package com.discord instead of apk package name
        Patcher.addPatch(ModelEmojiUnicode.class, "getImageUri", new Class<?>[]{ String.class, Context.class },
            new PinePatchFn(callFrame -> {
                String name = "emoji_" + callFrame.args[0];
                callFrame.setResult("res:///" + Utils.getResId(name, "raw"));
            })
        );

        // add stacktraces in debug logs page
        try {
            Class<WidgetDebugging.Adapter.Item> c = WidgetDebugging.Adapter.Item.class;
            Field debugItemBinding = c.getDeclaredField("binding");
            debugItemBinding.setAccessible(true);

            Patcher.addPatch(c, "onConfigure", new Class<?>[]{ int.class, AppLog.LoggedItem.class }, new PinePatchFn(callFrame -> {
                AppLog.LoggedItem loggedItem = (AppLog.LoggedItem) callFrame.args[1];
                Throwable th = loggedItem.l;
                if (th != null) try {
                    TextView logMessage = ((WidgetDebuggingAdapterItemBinding) debugItemBinding.get(callFrame.thisObject)).b;
                    SpannableStringBuilder sb = new SpannableStringBuilder("\n  at ");
                    StackTraceElement[] s = th.getStackTrace();
                    sb.append(TextUtils.join("\n  at ", s.length > 12 ? Arrays.copyOfRange(s, 0, 12) : s));
                    sb.setSpan(new AbsoluteSizeSpan(12, true), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    logMessage.append(sb);
                } catch (Throwable e) { logger.error(e); }
            }));
        } catch (Throwable e) { logger.error(e); }

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    String badPlugin = null;
                    for (StackTraceElement ele : throwable.getStackTrace()) {
                        String className = ele.getClassName();
                        if (!className.startsWith("com.aliucord.plugins.")) continue;
                        String plugin = className.replace("com.aliucord.plugins.", "").replaceAll("\\..+", "");
                        if (PluginManager.plugins.containsKey(plugin)) {
                            badPlugin = plugin;
                            SettingsUtils.setBool(PluginManager.getPluginPrefKey(plugin), false);
                            break;
                        }
                    }
                    File folder = new File(Constants.CRASHLOGS_PATH);
                    if (folder.exists() || folder.mkdir()) {
                        File file = new File(folder, new Timestamp(System.currentTimeMillis()).toString().replaceAll(":", "_") + ".txt");
                        try (PrintStream ps = new PrintStream(file)) {
                            throwable.printStackTrace(ps);
                        } catch (FileNotFoundException ignored) {}
                    }

                    String moreInfo = badPlugin != null ?
                            String.format("This crash was caused by %s, so I automatically disabled it for you.", badPlugin) :
                            "Check the crashes section in the settings for more info";
                    Toast.makeText(Utils.getAppContext(),"An unrecoverable crash occurred. " + moreInfo, Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }.start();
            try {
                Thread.sleep(4200); // Wait for toast to end
            } catch (InterruptedException ignored) {}
            System.exit(2);
        });

        CorePlugins.startAll(activity);

        if (loadedPlugins) startAllPlugins();
    }

    private static void loadAllPlugins(Context context) {
        File dir = new File(Constants.BASE_PATH + "/plugins");
        if (!dir.exists()) {
            boolean res = dir.mkdirs();
            if (!res) logger.error("Failed to create directories!", null);
        }
        for (File f : Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(".zip"))))
            PluginManager.loadPlugin(context, f);
        loadedPlugins = true;
    }

    private static void startAllPlugins() {
        for (String name : PluginManager.plugins.keySet()) {
            try {
                if (PluginManager.isPluginEnabled(name))
                    PluginManager.startPlugin(name);
            } catch (Throwable e) {
                PluginManager.logger.error("Exception while starting plugin: " + name, e);
                PluginManager.stopPlugin(name);
            }
        }

        Utils.threadPool.execute(() -> PluginUpdater.checkUpdates(true));
    }

    private static boolean checkPermissions(AppCompatActivity activity) {
        String perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (activity.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) return true;
        activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted == Boolean.TRUE) {
                loadAllPlugins(activity);
                startAllPlugins();
            } else Toast.makeText(activity, "You have to grant storage permission to load plugins", Toast.LENGTH_LONG).show();
        }).launch(perm);
        return false;
    }
}
