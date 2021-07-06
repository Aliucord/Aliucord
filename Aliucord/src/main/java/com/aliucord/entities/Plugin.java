/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.NonNull;

import com.aliucord.api.CommandsAPI;
import com.aliucord.api.PatcherAPI;
import com.aliucord.api.SettingsAPI;
import com.discord.app.AppBottomSheet;
import com.discord.app.AppFragment;

@SuppressWarnings("unused")
public abstract class Plugin {
    public static class Manifest {
        public static class Author {
            public String name;
            public long id;

            public Author(String name) {
                this(name, 0);
            }
            public Author(String name, long id) {
                this.name = name;
                this.id = id;
            }

            @NonNull
            @Override
            public String toString() { return name; }
        }

        public Author[] authors = new Author[]{};
        public String description = "";
        public String version = "1.0.0";
        // TODO: public String discord;
        public String updateUrl;
    }

    public static class SettingsTab {
        public enum Type { PAGE, BOTTOM_SHEET }

        public interface SettingsPage {
            void onViewBound(View view);
        }

        public Type type;
        public Class<? extends AppFragment> page;
        public Class<AppBottomSheet> bottomSheet;
        public boolean needsPlugin;
        public Object[] args;

        // TODO: public boolean addTab = false;

        public SettingsTab(Class<? extends AppFragment> settings) {
            type = Type.PAGE;
            page = settings;
        }

        @SuppressWarnings("unchecked")
        public SettingsTab(Class<?> settings, Type type) {
            this.type = type;
            if (type == Type.PAGE) page = (Class<? extends AppFragment>) settings;
            else bottomSheet = (Class<AppBottomSheet>) settings;
        }

        /** Sets the constructor args that will be passed to this SettingsTab */
        public SettingsTab withArgs(Object... args) {
            this.args = args;
            return this;
        }
    }

    @NonNull
    public abstract Manifest getManifest();

    public void load(Context context) throws Throwable {}
    @SuppressWarnings("RedundantThrows")
    public void unload(Context context) throws Throwable {} // not used now

    public abstract void start(Context context) throws Throwable;
    public abstract void stop(Context context) throws Throwable;

    public String name = this.getClass().getSimpleName();

    public SettingsTab settingsTab;

    public Resources resources;
    public boolean needsResources = false;

    public String __filename;

    // api
    protected CommandsAPI commands = new CommandsAPI(name);
    protected PatcherAPI patcher = new PatcherAPI();
    public SettingsAPI settings = new SettingsAPI(name);
}
