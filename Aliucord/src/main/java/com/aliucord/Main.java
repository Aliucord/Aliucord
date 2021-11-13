/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.text.*;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.*;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.coreplugins.CorePlugins;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;
import com.aliucord.settings.*;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.utils.ChangelogUtils;
import com.aliucord.views.Divider;
import com.aliucord.views.ToolbarButton;
import com.discord.app.AppActivity;
import com.discord.app.AppLog;
import com.discord.databinding.WidgetChangeLogBinding;
import com.discord.databinding.WidgetDebuggingAdapterItemBinding;
import com.discord.models.domain.emoji.ModelEmojiUnicode;
import com.discord.stores.StoreInviteSettings;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.changelog.WidgetChangeLog;
import com.discord.widgets.chat.list.WidgetChatList;
import com.discord.widgets.debugging.WidgetDebugging;
import com.discord.widgets.guilds.invite.WidgetGuildInvite;
import com.discord.widgets.settings.WidgetSettings;
import com.lytefast.flexinput.R;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;

import dalvik.system.PathClassLoader;

@SuppressWarnings({ "ConstantConditions", "unused" })
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

        Patcher.addPatch(AppActivity.class, "onCreate", new Class<?>[] { Bundle.class }, new Hook(param ->
            Utils.appActivity = (AppActivity) param.thisObject));

        try {
            Patcher.addPatch(WidgetChatList.class.getDeclaredConstructor(), new Hook(param ->
                Utils.widgetChatList = (WidgetChatList) param.thisObject));
        } catch (Exception e) {
            Patcher.logger.error(e);
        }
    }

    /** Aliucord's init hook. Plugins are started here */
    @SuppressLint("SetTextI18n")
    public static void init(AppActivity activity) {
        if (initialized) return;
        initialized = true;

        Patcher.addPatch(WidgetSettings.class, "onViewBound", new Class<?>[]{ View.class }, new Hook(param -> {
            Context context = ((WidgetSettings) param.thisObject).requireContext();
            CoordinatorLayout view = (CoordinatorLayout) param.args[0];
            LinearLayoutCompat v = (LinearLayoutCompat) ((NestedScrollView)
                view.getChildAt(1)).getChildAt(0);

            int baseIndex = v.indexOfChild(v.findViewById(Utils.getResId("developer_options_divider", "id")));
            v.addView(new Divider(context), baseIndex);
            TextView header = new TextView(context, null, 0, R.i.UiKit_Settings_Item_Header);
            header.setText("Aliucord");
            header.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
            v.addView(header, baseIndex + 1);

            Typeface font = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium);
            TextView plugins = new TextView(context, null, 0, R.i.UiKit_Settings_Item_Icon);
            plugins.setText("Plugins");
            plugins.setTypeface(font);
            int iconColor = ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal);
            Drawable icon = ContextCompat.getDrawable(context, R.e.ic_clear_all_white_24dp);
            if (icon != null) {
                Drawable copy = icon.mutate();
                copy.setTint(iconColor);
                plugins.setCompoundDrawablesRelativeWithIntrinsicBounds(copy, null, null, null);
            }
            plugins.setOnClickListener(e -> Utils.openPage(e.getContext(), Plugins.class));
            v.addView(plugins, baseIndex + 2);

            TextView updater = new TextView(context, null, 0, R.i.UiKit_Settings_Item_Icon);
            updater.setText("Updater");
            updater.setTypeface(font);
            icon = ContextCompat.getDrawable(context, R.e.ic_file_download_white_24dp);
            if (icon != null) {
                Drawable copy = icon.mutate();
                copy.setTint(iconColor);
                updater.setCompoundDrawablesRelativeWithIntrinsicBounds(copy, null, null, null);
            }
            updater.setOnClickListener(e -> Utils.openPage(e.getContext(), Updater.class));
            v.addView(updater, baseIndex + 3);

            TextView crashes = new TextView(context, null, 0, R.i.UiKit_Settings_Item_Icon);
            crashes.setText("Crashes");
            crashes.setTypeface(font);
            icon = ContextCompat.getDrawable(context, R.e.ic_history_white_24dp);
            if (icon != null) {
                Drawable copy = icon.mutate();
                copy.setTint(iconColor);
                crashes.setCompoundDrawablesRelativeWithIntrinsicBounds(copy, null, null, null);
            }
            crashes.setOnClickListener(e -> Utils.openPage(e.getContext(), Crashes.class));
            v.addView(crashes, baseIndex + 4);

            var debug = new TextView(context, null, 0, R.i.UiKit_Settings_Item_Icon);
            debug.setText("Open Debug Log");
            debug.setTypeface(font);
            icon = ContextCompat.getDrawable(context, R.e.ic_audit_logs_24dp);
            if (icon != null) {
                Drawable copy = icon.mutate();
                copy.setTint(iconColor);
                debug.setCompoundDrawablesRelativeWithIntrinsicBounds(copy, null, null, null);
            }
            debug.setOnClickListener(e -> Utils.openPage(e.getContext(), WidgetDebugging.class));
            v.addView(debug, baseIndex + 5);

            TextView version = v.findViewById(Utils.getResId("app_info_header", "id"));
            boolean isDebuggable = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            version.setText(version.getText() + " | Aliucord " + BuildConfig.GIT_REVISION + (isDebuggable ? " (debuggable)" : ""));

            TextView uploadLogs = v.findViewById(Utils.getResId("upload_debug_logs", "id"));
            uploadLogs.setText("Aliucord Support Server");
            uploadLogs.setOnClickListener(e ->
                WidgetGuildInvite.Companion.launch(e.getContext(), new StoreInviteSettings.InviteCode(Constants.ALIUCORD_SUPPORT, "", null)));
        }));

        // Patch to repair built-in emotes is needed because installer doesn't recompile resources,
        // so they stay in package com.discord instead of apk package name
        Patcher.addPatch(ModelEmojiUnicode.class, "getImageUri", new Class<?>[]{String.class, Context.class},
                new InsteadHook(param -> "res:///" + Utils.getResId("emoji_" + param.args[0], "raw"))
        );

        // Patch to allow changelogs without media
        Patcher.addPatch(WidgetChangeLog.class, "configureMedia", new Class<?>[]{String.class}, new PreHook(param -> {
            WidgetChangeLog _this = (WidgetChangeLog) param.thisObject;
            String media = _this.getMostRecentIntent().getStringExtra("INTENT_EXTRA_VIDEO");

            if (media == null) {
                WidgetChangeLogBinding binding = WidgetChangeLog.access$getBinding$p(_this);
                binding.i.setVisibility(View.GONE); // changeLogVideoOverlay
                binding.h.setVisibility(View.GONE); // changeLogVideo
                param.setResult(null);
            }
        }));

        // Patch for custom footer actions
        Patcher.addPatch(WidgetChangeLog.class, "configureFooter", new Class<?>[0], new PreHook(param -> {
            WidgetChangeLog _this = (WidgetChangeLog) param.thisObject;
            WidgetChangeLogBinding binding = WidgetChangeLog.access$getBinding$p(_this);

            Parcelable[] actions = _this.getMostRecentIntent().getParcelableArrayExtra("INTENT_EXTRA_FOOTER_ACTIONS");

            if (actions == null) {
                return;
            }

            AppCompatImageButton twitterButton = binding.g;
            LinearLayout parent = (LinearLayout) twitterButton.getParent();

            parent.removeAllViewsInLayout();

            for (Parcelable parcelable : actions) {
                ChangelogUtils.FooterAction action = ((ChangelogUtils.FooterAction) parcelable);

                ToolbarButton button = new ToolbarButton(parent.getContext());
                button.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), action.getDrawableResourceId()), false);

                button.setPadding(twitterButton.getPaddingLeft(), twitterButton.getPaddingTop(), twitterButton.getPaddingRight(), twitterButton.getPaddingBottom());
                button.setLayoutParams(twitterButton.getLayoutParams());

                button.setOnClickListener(v -> Utils.launchUrl(action.getUrl()));

                parent.addView(button);
            }

            param.setResult(null);
        }));

        // add stacktraces in debug logs page
        try {
            Class<WidgetDebugging.Adapter.Item> c = WidgetDebugging.Adapter.Item.class;
            Field debugItemBinding = c.getDeclaredField("binding");
            debugItemBinding.setAccessible(true);

            Patcher.addPatch(c, "onConfigure", new Class<?>[]{ int.class, AppLog.LoggedItem.class }, new Hook(param -> {
                AppLog.LoggedItem loggedItem = (AppLog.LoggedItem) param.args[1];
                Throwable th = loggedItem.l;
                if (th != null) try {
                    TextView logMessage = ((WidgetDebuggingAdapterItemBinding) debugItemBinding.get(param.thisObject)).b;
                    SpannableStringBuilder sb = new SpannableStringBuilder("\n  at ");
                    StackTraceElement[] s = th.getStackTrace();
                    sb.append(TextUtils.join("\n  at ", s.length > 12 ? Arrays.copyOfRange(s, 0, 12) : s));
                    sb.setSpan(new AbsoluteSizeSpan(12, true), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    logMessage.append(sb);
                } catch (Throwable e) { logger.error(e); }
            }));
        } catch (Throwable e) { logger.error(e); }

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (Looper.getMainLooper().getThread() != thread) {
                logger.error("Uncaught exception on thread " + thread.getName(), throwable);
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    String badPlugin = null;
                    boolean disabledPlugin = false;
                    for (StackTraceElement ele : throwable.getStackTrace()) {
                        String className = ele.getClassName();

                        for (Map.Entry<PathClassLoader, Plugin> entry : PluginManager.classLoaders.entrySet()) {
                            try {
                                var loadedClass = entry.getKey().loadClass(className);
                                if (!loadedClass.getClassLoader().equals(entry.getKey())) {
                                    // class was loaded from the parent classloader, ignore
                                    continue;
                                }

                                badPlugin = entry.getValue().getName();
                                if (SettingsUtils.getBool(CrashesKt.autoDisableKey, true)) {
                                    disabledPlugin = true;
                                    SettingsUtils.setBool(PluginManager.getPluginPrefKey(badPlugin), false);
                                }
                                break;
                            } catch (ClassNotFoundException ignored) {
                            }
                        }

                        if (badPlugin != null) {
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

                    var sb = new StringBuilder("An unrecoverable crash occurred. ");
                    if (badPlugin != null) {
                        sb.append("This crash was caused by ").append(badPlugin);
                        if (disabledPlugin) {
                            sb.append(", so I automatically disabled it for you");
                        }
                        sb.append(". ");
                    }
                    sb.append("Check the crashes section in the settings for more info.");

                    Toast.makeText(Utils.getAppContext(), sb.toString(), Toast.LENGTH_LONG).show();
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
            if (!res) {
                logger.error("Failed to create directories!", null);
                return;
            }
        }

        File[] sortedPlugins = dir.listFiles();
        Arrays.sort(sortedPlugins, Comparator.comparing(File::getName));

        for (File f : sortedPlugins) {
            var name = f.getName();
            if (name.endsWith(".zip")) {
                PluginManager.loadPlugin(context, f);
            } else if (!name.equals("oat")) { // Some roms create this
                if (f.isDirectory()) {
                    Utils.showToast(String.format("Found directory %s in your plugins folder. DO NOT EXTRACT PLUGIN ZIPS!", name), true);
                } else if (name.equals("classes.dex") || name.endsWith(".json")) {
                    Utils.showToast(String.format("Found extracted plugin file %s in your plugins folder. DO NOT EXTRACT PLUGIN ZIPS!", name), true);
                }
                rmrf(f);
            }
        }
        loadedPlugins = true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void rmrf(File file) {
        if (file.isDirectory()) {
            for (var child : file.listFiles())
                rmrf(child);
        }
        file.delete();
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
