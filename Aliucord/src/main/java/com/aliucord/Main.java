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
import android.graphics.Typeface;
import android.os.*;
import android.text.*;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.aliucord.coreplugins.CorePlugins;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;
import com.aliucord.settings.*;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.utils.ChangelogUtils;
import com.aliucord.views.Divider;
import com.aliucord.views.ToolbarButton;
import com.discord.app.*;
import com.discord.databinding.WidgetChangeLogBinding;
import com.discord.databinding.WidgetDebuggingAdapterItemBinding;
import com.discord.models.domain.emoji.ModelEmojiUnicode;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.changelog.WidgetChangeLog;
import com.discord.widgets.chat.list.WidgetChatList;
import com.discord.widgets.debugging.WidgetDebugging;
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

    public static SettingsUtilsJSON settings;

    /** Aliucord's preInit hook. Plugins are loaded here */
    public static void preInit(AppActivity activity) throws NoSuchMethodException {
        if (preInitialized) return;
        preInitialized = true;

        Utils.appActivity = activity;

        if (checkPermissions(activity)) preInitWithPermissions(activity);

        Patcher.addPatch(AppActivity.class, "onCreate", new Class<?>[]{ Bundle.class }, new Hook(param ->
            Utils.appActivity = (AppActivity) param.thisObject));

        Patcher.addPatch(WidgetChatList.class.getDeclaredConstructor(), new Hook(param ->
            Utils.widgetChatList = (WidgetChatList) param.thisObject));
    }

    private static void preInitWithPermissions(AppCompatActivity activity) {
        settings = new SettingsUtilsJSON("Aliucord");
        SettingsUtilsJSON.Companion.migrateAliucordSettings(settings);
        CorePlugins.loadAll(activity);
        loadAllPlugins(activity);
    }

    /** Aliucord's init hook. Plugins are started here */
    @SuppressLint("SetTextI18n")
    public static void init(AppActivity activity) {
        if (initialized) return;
        initialized = true;

        Patcher.addPatch(WidgetSettings.class, "onViewBound", new Class<?>[]{ View.class }, new Hook(param -> {
            ViewGroup layout = Utils.nestedChildAt((ViewGroup) param.args[0], 1, 0);
            Context context = layout.getContext();

            int baseIndex = layout.indexOfChild(layout.findViewById(Utils.getResId("developer_options_divider", "id")));

            layout.addView(new Divider(context), baseIndex++);

            var header = new TextView(context, null, 0, R.i.UiKit_Settings_Item_Header);
            header.setText("Aliucord");
            header.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
            layout.addView(header, baseIndex++);

            var font = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium);

            layout.addView(
                makeSettingsEntry(font, context, "Settings", R.e.ic_behavior_24dp, AliucordPage.class),
                baseIndex++
            );
            layout.addView(
                makeSettingsEntry(font, context, "Plugins", R.e.ic_clear_all_white_24dp, Plugins.class),
                baseIndex++
            );
            layout.addView(
                makeSettingsEntry(font, context, "Updater", R.e.ic_file_download_white_24dp, Updater.class),
                baseIndex++
            );
            layout.addView(
                makeSettingsEntry(font, context, "Crashes", R.e.ic_history_white_24dp, Crashes.class),
                baseIndex++
            );
            layout.addView(
                makeSettingsEntry(font, context, "Open Debug Log", R.e.ic_audit_logs_24dp, WidgetDebugging.class),
                baseIndex
            );

            TextView versionView = layout.findViewById(Utils.getResId("app_info_header", "id"));
            var text = versionView.getText() + " | Aliucord " + BuildConfig.GIT_REVISION;
            if (Utils.isDebuggable()) text += " [DEBUGGABLE]";
            versionView.setText(text);

            TextView uploadLogs = layout.findViewById(Utils.getResId("upload_debug_logs", "id"));
            uploadLogs.setText("Aliucord Support Server");
            uploadLogs.setOnClickListener(e -> Utils.joinSupportServer(e.getContext()));
        }));

        // Patch to repair built-in emotes is needed because installer doesn't recompile resources,
        // so they stay in package com.discord instead of apk package name
        Patcher.addPatch(ModelEmojiUnicode.class, "getImageUri", new Class<?>[]{ String.class, Context.class },
            new InsteadHook(param -> "res:///" + Utils.getResId("emoji_" + param.args[0], "raw"))
        );

        // Patch to allow changelogs without media
        Patcher.addPatch(WidgetChangeLog.class, "configureMedia", new Class<?>[]{ String.class }, new PreHook(param -> {
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
                Throwable th = loggedItem.m;
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

        Thread.setDefaultUncaughtExceptionHandler(Main::crashHandler);

        if (loadedPlugins) {
            CorePlugins.startAll(activity);
            startAllPlugins();
        }
    }

    private static void crashHandler(Thread thread, Throwable throwable) {
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
                            if (Main.settings.getBool(AliucordPageKt.AUTO_DISABLE_ON_CRASH_KEY, true)) {
                                disabledPlugin = true;
                                Main.settings.setBool(PluginManager.getPluginPrefKey(badPlugin), false);
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
    }

    private static TextView makeSettingsEntry(Typeface font, Context context, String text, @DrawableRes int resId, Class<? extends AppComponent> component) {
        var view = new TextView(context, null, 0, R.i.UiKit_Settings_Item_Icon);
        view.setText(text);
        view.setTypeface(font);
        var icon = ContextCompat.getDrawable(context, resId);
        if (icon != null) {
            icon.mutate().setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal));
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        }
        view.setOnClickListener(e -> Utils.openPage(e.getContext(), component));
        return view;
    }

    private static void loadAllPlugins(Context context) {
        File dir = new File(Constants.PLUGINS_PATH);
        if (!dir.exists()) {
            boolean res = dir.mkdirs();
            if (!res) {
                logger.error("Failed to create directories!", null);
                return;
            }
        }

        File[] sortedPlugins = dir.listFiles();
        // Always sort plugins alphabetically for reproducible results
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
        if (!PluginManager.failedToLoad.isEmpty())
            Utils.showToast("Some plugins failed to load. Check the plugins page for more info.");
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
                preInitWithPermissions(activity);
                CorePlugins.startAll(activity);
                startAllPlugins();
            } else Toast.makeText(activity, "You have to grant storage permission to use Aliucord", Toast.LENGTH_LONG).show();
        }).launch(perm);
        return false;
    }
}
