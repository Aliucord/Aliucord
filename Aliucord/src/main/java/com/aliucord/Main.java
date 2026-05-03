/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.text.style.AbsoluteSizeSpan;
import android.view.*;
import android.widget.*;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.aliucord.api.NotificationsAPI;
import com.aliucord.entities.*;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.patcher.*;
import com.aliucord.screens.UpdaterScreen;
import com.aliucord.settings.*;
import com.aliucord.updater.*;
import com.aliucord.utils.ChangelogUtils;
import com.aliucord.utils.ReflectUtils;
import com.aliucord.views.Divider;
import com.aliucord.views.ToolbarButton;
import com.aliucord.wrappers.embeds.MessageEmbedWrapper;
import com.discord.api.message.embed.EmbedField;
import com.discord.app.*;
import com.discord.databinding.*;
import com.discord.models.domain.*;
import com.discord.models.domain.emoji.ModelEmojiCustom;
import com.discord.models.domain.emoji.ModelEmojiUnicode;
import com.discord.stores.*;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.guildautomod.AutoModUtils;
import com.discord.utilities.user.UserUtils;
import com.discord.widgets.changelog.WidgetChangeLog;
import com.discord.widgets.chat.input.SmoothKeyboardReactionHelper;
import com.discord.widgets.chat.list.WidgetChatList;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemAutoModSystemMessageEmbed;
import com.discord.widgets.chat.list.entries.AutoModSystemMessageEmbedEntry;
import com.discord.widgets.chat.list.entries.ChatListEntry;
import com.discord.widgets.debugging.WidgetDebugging;
import com.discord.widgets.guilds.profile.WidgetChangeGuildIdentity;
import com.discord.widgets.guilds.profile.WidgetGuildProfileSheet$configureGuildActions$$inlined$apply$lambda$4;
import com.discord.widgets.home.WidgetHome;
import com.discord.widgets.settings.WidgetSettings;
import com.discord.widgets.settings.profile.WidgetEditUserOrGuildMemberProfile;
import com.discord.widgets.status.*;
import com.lytefast.flexinput.R;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;

import dalvik.system.PathClassLoader;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;

public final class Main {
    /** Whether Aliucord has been preInitialized */
    public static boolean preInitialized = false;
    /** Whether Aliucord has been initialized */
    public static boolean initialized = false;
    public static final Logger logger = new Logger();

    private static boolean loadedPlugins;

    public static SettingsUtilsJSON settings;

    /**
     * Aliucord's preInit hook. Plugins are loaded here
     * @noinspection unused
     */
    public static void preInit(AppActivity activity) throws NoSuchMethodException {
        if (preInitialized) return;
        preInitialized = true;

        Utils.appActivity = activity;

        if (checkPermissions(activity)) preInitWithPermissions(activity);

        // Since 1.4.0, this is implemented via a smali patch to ensure it works even if Aliucord failed to load
        if (!ManagerBuild.hasPatches("1.4.0")) {
            // Fix 2025-04-03 gateway change that ported visual refresh theme names over the legacy user settings
            // Theme entries like "darker" and "midnight" are unsupported
            Patcher.addPatch(ModelUserSettings.class, "assignField", new Class<?>[]{ Model.JsonReader.class }, new Hook(param -> {
                var $this = (ModelUserSettings) param.thisObject;

                switch ($this.getTheme()) {
                    case null:
                    case ModelUserSettings.THEME_DARK:
                    case ModelUserSettings.THEME_LIGHT:
                    case ModelUserSettings.THEME_PURE_EVIL:
                        return;
                    default:
                        try {
                            ReflectUtils.setField($this, "theme", "dark");
                        } catch (Exception e) {
                            logger.error("Failed to fix ModelUserSettings theme", e);
                        }
                }
            }));
        }

        Patcher.addPatch(AppActivity.class, "onCreate", new Class<?>[]{ Bundle.class }, new Hook(param -> {
            Utils.appActivity = (AppActivity) param.thisObject;
        }));

        Patcher.addPatch(WidgetChatList.class.getDeclaredConstructor(), new Hook(param -> {
            Utils.widgetChatList = (WidgetChatList) param.thisObject;
        }));
    }

    private static void preInitWithPermissions(AppCompatActivity activity) {
        createDirectories();
        settings = new SettingsUtilsJSON("Aliucord");
        PluginManager.loadCorePlugins(activity);
        loadAllPlugins(activity);
    }

    private static void createDirectories() {
        var settingsDir = new File(Constants.SETTINGS_PATH);
        if (!settingsDir.exists() && !settingsDir.mkdirs()) {
            throw new RuntimeException(new IOException("Failed to create Aliucord settings directory"));
        }

        var pluginsDir = new File(Constants.PLUGINS_PATH);
        if (!pluginsDir.exists() && !pluginsDir.mkdirs()) {
            throw new RuntimeException(new IOException("Failed to create Aliucord plugins directory"));
        }
    }

    /**
     * Aliucord's init hook. Plugins are started here
     * @noinspection unused
     */
    public static void init(AppActivity activity) {
        if (initialized) return;
        initialized = true;

        Thread.setDefaultUncaughtExceptionHandler(Main::crashHandler);

        if (loadedPlugins) {
            PluginManager.startCorePlugins();
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

    private static void loadAllPlugins(Context context) {
        if (PluginManager.isSafeModeEnabled()) {
            logger.warn("Safe mode is enabled. skipping loading external plugins");
            loadedPlugins = true;
            return;
        }
        File dir = new File(Constants.PLUGINS_PATH);
        if (!dir.exists()) {
            boolean res = dir.mkdirs();
            if (!res) {
                logger.error("Failed to create directories!", null);
                return;
            }
        }

        File[] sortedPlugins = dir.listFiles();
        if (sortedPlugins != null) {
            // Always sort plugins alphabetically for reproducible results
            Arrays.sort(sortedPlugins, Comparator.comparing(File::getName));

            for (File f : sortedPlugins) {
                var name = f.getName();
                if (name.endsWith(".zip")) {
                    PluginManager.loadPlugin(context, f);
                } else if (!name.equals("oat")) { // Some roms create this
                    if (f.isDirectory()) {
                        Utils.showToast(
                            String.format("Found directory %s in your plugins folder. DO NOT EXTRACT PLUGIN ZIPS!", name),
                            true
                        );
                    } else if (name.equals("classes.dex") || name.endsWith(".json")) {
                        Utils.showToast(
                            String.format("Found extracted plugin file %s in your plugins folder. DO NOT EXTRACT PLUGIN ZIPS!", name),
                            true
                        );
                    }
                    FilesKt.deleteRecursively(f);
                }
            }

            if (!PluginManager.failedToLoad.isEmpty())
                Utils.showToast("Some plugins failed to load. Check the plugins page for more info.");
        }
        loadedPlugins = true;
    }

    private static void startAllPlugins() {
        for (Map.Entry<String, Plugin> entry : PluginManager.plugins.entrySet()) {
            // coreplugins are started separately
            if (entry.getValue() instanceof CorePlugin) continue;

            var name = entry.getKey();
            try {
                if (PluginManager.isPluginEnabled(name))
                    PluginManager.startPlugin(name);
            } catch (Throwable e) {
                PluginManager.logger.error("Exception while starting plugin: " + name, e);
                PluginManager.stopPlugin(name);
            }
        }

        Utils.threadPool.execute(() -> {
            if (CoreUpdater.isUpdaterDisabled()) return;
            CoreUpdater.checkForUpdates();

            checkForPluginUpdates();
        });
    }

    private static void checkForPluginUpdates() {
        var updates = PluginUpdater.fetchUpdates(new PluginUpdaterSource());
        if (updates.isEmpty()) return;

        if (!PluginUpdater.isAutoUpdateEnabled()) {
            var notificationData = new NotificationData()
                .setTitle("Updater")
                .setBody(String.format("Found %s available plugin updates! Click to view...", updates.size()))
                .setAutoDismissPeriodSecs(30)
                .setOnClick((view) -> {
                    Utils.openPage(view.getContext(), UpdaterScreen.class);
                    return Unit.a;
                });

            NotificationsAPI.display(notificationData);
            return;
        }

        var failed = 0;
        var succeeded = new ArrayList<String>();
        for (var update : updates) {
            if (!update.isUpdatePossible()) continue;
            if (PluginUpdater.updatePlugin(update)) {
                succeeded.add(update.getPluginName());
            } else {
                failed++;
            }
        }

        var notification = new NotificationData()
            .setTitle("Updater");
        if (failed > 0) {
            notification
                .setAutoDismissPeriodSecs(30)
                .setBody(String.format("Failed to update %s plugins! Click to view...", failed))
                .setOnClick((view) -> {
                    Utils.openPage(view.getContext(), UpdaterScreen.class);
                    return Unit.a;
                });
        } else {
            notification
                .setAutoDismissPeriodSecs(10)
                .setOnClick((view) -> Unit.a)
                .setBody("Automatically updated plugins: "
                    + String.join(", ", CollectionsKt.take(succeeded, 5))
                    + (succeeded.size() > 5 ? String.format(", and %s others.", succeeded.size()) : ""));
        }
        NotificationsAPI.display(notification);
    }

    private static void permissionGrantedCallback(AppCompatActivity activity, boolean granted) {
        if (granted) {
            preInitWithPermissions(activity);
            PluginManager.startCorePlugins();
            startAllPlugins();
        } else Toast.makeText(activity, "You have to grant storage permission to use Aliucord", Toast.LENGTH_LONG).show();
    }

    private static boolean checkPermissions(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageLegacy()) {
            if (Environment.isExternalStorageManager()) return true;
            Toast.makeText(
                activity,
                "Please grant all files permission, so Aliucord can access its folder in Internal Storage",
                Toast.LENGTH_LONG
            ).show();
            activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> permissionGrantedCallback(activity, Environment.isExternalStorageManager())
            ).launch(
                new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:" + activity.getPackageName()))
            );
            return false;
        }

        var perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (activity.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) return true;
        activity.registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> permissionGrantedCallback(activity, granted)
        ).launch(perm);
        return false;
    }
}
