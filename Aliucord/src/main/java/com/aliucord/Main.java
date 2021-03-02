package com.aliucord;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.coreplugins.*;
import com.aliucord.fragments.MissingPatchesDialog;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PrePatchRes;
import com.aliucord.settings.Plugins;
import com.aliucord.settings.Updater;
import com.aliucord.updater.PluginUpdater;
import com.aliucord.views.Divider;
import com.discord.app.AppActivity;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.guilds.invite.WidgetGuildInvite;
import com.discord.widgets.settings.WidgetSettings;
import com.google.gson.reflect.TypeToken;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$d;
import com.lytefast.flexinput.R$h;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

import dalvik.system.PathClassLoader;

@SuppressWarnings({"unchecked", "deprecation"})
public class Main {
    public static boolean preInitialized = false;
    public static boolean initialized = false;
    public static Logger logger = new Logger();

    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> classes = new HashMap<String, List<String>>(){{
            put("com.discord.app.AppActivity", Collections.singletonList("*"));
            put("com.discord.app.AppActivity$b", Collections.singletonList("*"));
            put("com.discord.app.AppActivity$d", Collections.singletonList("*"));
            put("com.discord.widgets.settings.WidgetSettings", Collections.singletonList("onViewBound"));
            put("com.discord.models.domain.emoji.ModelEmojiUnicode", Collections.singletonList("getImageUri"));

            putAll(CommandHandler.getClassesToPatch());
            putAll(CoreCommands.getClassesToPatch());
            putAll(NotificationHandler.getClassesToPatch());
            putAll(NoTrack.getClassesToPatch());
            putAll(TokenLogin.getClassesToPatch());
        }};
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Aliucord/plugins";
        File dir = new File(path);
        if (!dir.exists()) return classes;
        for (File f : Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(".apk")))) {
            PathClassLoader loader = new PathClassLoader(f.getAbsolutePath(), Main.class.getClassLoader());
            String name = f.getName().replace(".apk", "");
            try {
                Map<String, List<String>> map = (Map<String, List<String>>) loader.loadClass("com.aliucord.plugins." + name)
                        .getMethod("getClassesToPatch").invoke(null);
                if (map == null) continue;

                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    String key = entry.getKey();
                    List<String> val = entry.getValue();
                    List<String> list = classes.get(key);
                    if (list == null) classes.put(key, val);
                    else if (!list.contains("*")) {
                        if (list instanceof ArrayList) {
                            list.removeAll(val);
                            list.addAll(val);
                        } else {
                            list = new ArrayList<>(list);
                            list.removeAll(val);
                            list.addAll(val);
                            classes.put(key, list);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return classes;
    }

    public static void preInit(AppActivity activity) {
        if (preInitialized) return;
        preInitialized = true;

        PluginManager.loadCorePlugins(activity);

        if (checkPermissions(activity)) {
            File dir = new File(Constants.BASE_PATH + "/plugins");
            if (!dir.exists()) {
                boolean res = dir.mkdirs();
                if (!res) logger.error("Failed to create directories!", null);
            }
            for (File f : Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(".apk"))))
                PluginManager.loadPlugin(activity, f);
        }
    }

    @SuppressLint("SetTextI18n")
    public static void init(AppActivity activity) {
        if (initialized) return;
        initialized = true;

        Patcher.addPatch("com.discord.widgets.settings.WidgetSettings", "onViewBound", (_this, args, res) -> {
            Context context = ((WidgetSettings) _this).requireContext();
            CoordinatorLayout view = (CoordinatorLayout) args.get(0);
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

            return res;
        });

        // Patch to repair built-in emotes is needed because installer doesn't recompile resources,
        // so they stay in package com.discord instead of apk package name
        Patcher.addPrePatch("com.discord.models.domain.emoji.ModelEmojiUnicode", "getImageUri", (_this, args) -> {
            if (args.size() != 2) return new PrePatchRes(args);
            String name = "emoji_" + args.get(0);
            return new PrePatchRes(args, "res:///" + Utils.getResId(name, "raw"));
        });

        PluginManager.startCorePlugins(activity);

        for (String name : PluginManager.plugins.keySet()) {
            try {
                if (!PluginManager.isPluginEnabled(name)) continue;
                PluginManager.startPlugin(name);
            } catch (Throwable e) { PluginManager.logger.error("Exception while starting plugin: " + name, e); }
        }

        try {
            String ln;
            StringBuilder res = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(activity.getAssets().open("aliucord-patches.json")));
            while ((ln = reader.readLine()) != null) res.append(ln);
            reader.close();

            Map<String, List<String>> patchedClasses = Utils.fromJson(res.toString().trim(),
                    TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(List.class, String.class).getType()).getType());
            boolean missing = false;
            for (Map.Entry<String, List<String>> entry : getClassesToPatch().entrySet()) {
                List<String> patched = patchedClasses.get(entry.getKey());
                if (patched == null) {
                    missing = true;
                    break;
                }
                if (patched.size() > 0 && patched.get(0).equals("*")) continue;
                if (!patched.containsAll(entry.getValue())) {
                    missing = true;
                    break;
                }
            }
            if (missing) {
                logger.info("Detected possibly missing patches");
                new MissingPatchesDialog().show(activity.getSupportFragmentManager(), "MissingPatchesDialog");
            }
        } catch (Throwable e) { logger.error("Failed to check missing patches", e); }

        new Thread(() -> PluginUpdater.checkUpdates(true)).start();
    }

    private static boolean checkPermissions(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            String perm = "android.permission.WRITE_EXTERNAL_STORAGE";
            if (activity.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) return true;
//            Toast.makeText(
//                    activity,
//                    "Restart app after granting permission (permission is needed to load plugins)",
//                    Toast.LENGTH_LONG
//            ).show();
            activity.requestPermissions(new String[]{ perm }, 1);
            return false;
        }
        return true;
    }
}
