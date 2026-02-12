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

import com.aliucord.entities.CorePlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.patcher.*;
import com.aliucord.settings.*;
import com.aliucord.updater.ManagerBuild;
import com.aliucord.updater.PluginUpdater;
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
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;

import dalvik.system.PathClassLoader;
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

        Patcher.addPatch(AppActivity.class, "onCreate", new Class<?>[]{ Bundle.class }, new Hook(param -> {
            Utils.appActivity = (AppActivity) param.thisObject;
        }));

        Patcher.addPatch(WidgetChatList.class.getDeclaredConstructor(), new Hook(param -> {
            Utils.widgetChatList = (WidgetChatList) param.thisObject;
        }));
    }

    private static void preInitWithPermissions(AppCompatActivity activity) {
        settings = new SettingsUtilsJSON("Aliucord");
        PluginManager.loadCorePlugins(activity);
        loadAllPlugins(activity);
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

        Utils.threadPool.execute(() -> PluginUpdater.checkUpdates(true));
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
