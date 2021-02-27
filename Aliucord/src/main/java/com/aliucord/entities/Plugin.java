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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class Plugin {
    public static class Manifest {
        public static class Author {
            public String name = "";
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

    public static class Settings {
        public enum Type { PAGE, BOTTOMSHEET }

        public interface SettingsPage {
            void onViewBound(View view);
        }

        public Type type;
        public Class<? extends AppFragment> page;
        public Class<AppBottomSheet> bottomSheet;

        // TODO: public boolean addTab = false;

        public Settings(Class<? extends AppFragment> settings) {
            type = Type.PAGE;
            page = settings;
        }

        @SuppressWarnings("unchecked")
        public Settings(Class<?> settings, Type type) {
            this.type = type;
            if (type == Type.PAGE) page = (Class<? extends AppFragment>) settings;
            else bottomSheet = (Class<AppBottomSheet>) settings;
        }
    }

    public static Map<String, List<String>> getClassesToPatch() { return new HashMap<>(); }

    @NonNull
    public abstract Manifest getManifest();

    public void load(Context context) {}
    public void unload(Context context) {} // not used now

    public abstract void start(Context context);
    public abstract void stop(Context context);

    public String name = this.getClass().getSimpleName();

    public Settings settings;

    public Resources resources;
    public boolean needsResources = false;

    // api
    protected CommandsAPI commands = new CommandsAPI(name);
    protected PatcherAPI patcher = new PatcherAPI();
    public SettingsAPI sets = new SettingsAPI(name);
}
