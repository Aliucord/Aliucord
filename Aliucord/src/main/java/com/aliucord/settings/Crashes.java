/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Constants;
import com.aliucord.Utils;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.DangerButton;
import com.discord.simpleast.code.CodeNode;
import com.discord.simpleast.code.CodeNode$a;
import com.discord.utilities.textprocessing.Rules$createCodeBlockRule$codeStyleProviders$1;
import com.discord.utilities.textprocessing.node.BasicRenderContext;
import com.discord.utilities.textprocessing.node.BlockBackgroundNode;
import com.google.android.material.appbar.AppBarLayout;
import com.lytefast.flexinput.R$d;
import com.lytefast.flexinput.R$h;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Crashes extends SettingsPage {
    private static class CrashLog {
        public String timestamp;
        public String stacktrace;
        public int times;
    }

    private static class RenderContext implements BasicRenderContext {
        private final Context context;
        public RenderContext(Context ctx) {
            context = ctx;
        }

        @Override
        public Context getContext() {
            return context;
        }
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SetTextI18n")
    public void onViewBound(View view) {
        super.onViewBound(view);

        setActionBarTitle("Crash Logs");

        Context context = requireContext();
        int p = Utils.getDefaultPadding() / 2;
        File folder = new File(Constants.BASE_PATH, "crashlogs");
        File[] files = folder.listFiles();

        LinearLayout v = (LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0);
        v.setPadding(p, p, p, p);

        Toolbar toolbar = (Toolbar) ((AppBarLayout) ((CoordinatorLayout) view).getChildAt(0)).getChildAt(0);

        AppCompatImageButton crashFolderBtn = new AppCompatImageButton(context);
        int ic1 = R$d.ic_open_in_new_white_24dp;
        AppCompatImageButton clearLogsBtn = new AppCompatImageButton(context);
        int ic2 = R$d.ic_clear_all_white_24dp;

        Toolbar.LayoutParams crashFolderBtnParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        crashFolderBtnParams.gravity = Gravity.END;
        crashFolderBtnParams.setMarginEnd(p);
        crashFolderBtn.setLayoutParams(crashFolderBtnParams);
        Toolbar.LayoutParams clearLogsParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        clearLogsParams.gravity = Gravity.END;
        clearLogsBtn.setLayoutParams(clearLogsParams);
        crashFolderBtn.setPadding(p, p, p, p);
        clearLogsBtn.setPadding(p, p, p, p);

        crashFolderBtn.setBackgroundColor(Color.TRANSPARENT);
        clearLogsBtn.setBackgroundColor(Color.TRANSPARENT);
        if (files != null) crashFolderBtn.setClickable(true);

        Drawable openPluginsExternal = ContextCompat.getDrawable(context, ic1).mutate();
        openPluginsExternal.setAlpha(200);
        crashFolderBtn.setImageDrawable(openPluginsExternal);
        Drawable clearLogs = ContextCompat.getDrawable(context, ic2).mutate();
        clearLogs.setAlpha(200);
        clearLogsBtn.setImageDrawable(clearLogs);

        crashFolderBtn.setOnClickListener(e -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(String.valueOf(folder)), "resource/folder");
            startActivity(Intent.createChooser(intent, "Open folder"));
        });
        clearLogsBtn.setOnClickListener(e -> {
            if (files != null) {
                for (File file : files) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
            v.removeAllViews();
            clearLogsBtn.setClickable(false);
        });

        toolbar.addView(crashFolderBtn);
        toolbar.addView(clearLogsBtn);

        Map<Integer, CrashLog> crashes = getCrashes();
        if (crashes == null || crashes.size() == 0) {
            TextView header = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Header);
            header.setText("Woah, no crashes :O");
            header.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
            header.setGravity(Gravity.CENTER);

            DangerButton crashBtn = new DangerButton(context);
            crashBtn.setText("LET'S CHANGE THAT");
            crashBtn.setPadding(p, p, p, p);
            crashBtn.setOnClickListener(e -> {
                throw new RuntimeException("You fool...");
            });
            v.addView(header);
            v.addView(crashBtn);
        } else {
            TextView hint = new TextView(context, null, 0, R$h.UiKit_Settings_Item_SubText);
            hint.setText("Hint: You can find these crash logs in the Aliucord/crashlogs folder!");
            // v.addView(hint);

            for (CrashLog crash : crashes.values()) {
                TextView header = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Header);
                header.setText(crash.timestamp + (crash.times > 1 ? " (" + crash.times + ")" : ""));
                header.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));

                TextView body = new TextView(context);
                BlockBackgroundNode<BasicRenderContext> node = new BlockBackgroundNode<>(false, new CodeNode<BasicRenderContext>(
                        new CodeNode$a.b<>(crash.stacktrace), "", Rules$createCodeBlockRule$codeStyleProviders$1.INSTANCE
                ));
                SpannableStringBuilder builder = new SpannableStringBuilder();
                node.render(builder, new RenderContext(context));
                body.setText(builder);
                body.setOnClickListener(e -> {
                    Utils.setClipboard("CrashLog-" + crash.timestamp, crash.stacktrace);
                    Utils.showToast(context, "Copied to clipboard");
                });

                v.addView(header);
                v.addView(body);
            }

        }
    }

    @Nullable
    private Map<Integer, CrashLog> getCrashes() {
        File folder = new File(Constants.BASE_PATH, "crashlogs");
        File[] files = folder.listFiles();
        if (files == null) return null;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        Map<Integer, CrashLog> res = new HashMap<>();
        for (File file : files) {
            if (!file.isFile()) continue;
            int length;
            try { length = Math.toIntExact(file.length()); } catch (ArithmeticException ex) { continue; }
            byte[] buffer = new byte[length];
            try (FileInputStream is = new FileInputStream(file)) {
                //noinspection ResultOfMethodCallIgnored
                is.read(buffer);
            } catch (IOException ignored) {}
            String content = new String(buffer);

            int hashCode = content.hashCode();
            CrashLog existing = res.get(hashCode);
            if (existing != null) {
                existing.times += 1;
            } else {
                CrashLog crash = new CrashLog();
                crash.timestamp = file.getName().replace(".txt", "").replaceAll("_", ":");
                crash.stacktrace = content;
                crash.times = 1;
                res.put(hashCode, crash);
            }
        }
        return res;
    }
}
