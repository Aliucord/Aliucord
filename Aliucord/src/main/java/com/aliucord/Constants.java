/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.os.Environment;

import com.aliucord.utils.ReflectUtils;
import com.discord.stores.StoreStream;

@SuppressWarnings("unused")
public final class Constants {
    public static final class Icons {
        /** Clyde avatar */
        public static final String CLYDE = "https://canary.discord.com/assets/f78426a064bc9dd24847519259bc42af.png";
    }

    // Font resource ids, they're not defined by any generated package but they seem to be constant so i made this class.
    public static final class Fonts {
        private static final int base = 0x7f090000;

        public static final int ginto_bold = base;
        public static final int ginto_medium = base + 1;
        public static final int ginto_regular = base + 2;
        public static final int roboto_medium_numbers = base + 3;
        public static final int sourcecodepro_semibold = base + 4;
        public static final int whitney_bold = base + 5;
        public static final int whitney_medium = base + 6;
        public static final int whitney_semibold = base + 7;
    }

    /** Link to the Aliucord github repo */
    public static final String ALIUCORD_GITHUB_REPO = "https://github.com/Aliucord/Aliucord";
    /** Code of the Aliucord discord server */
    public static final String ALIUCORD_SUPPORT = "EsNDvBaHVU";
    public static final long ALIUCORD_GUILD_ID = 811255666990907402L;
    public static final long SUPPORT_CHANNEL_ID = 811261298997460992L; // #support
    public static final long PLUGIN_SUPPORT_CHANNEL_ID = 847566769258233926L; // #plugin-support
    public static final long PLUGIN_LINKS_CHANNEL_ID = 811275162715553823L; // #plugins-list
    public static final long PLUGIN_LINKS_UPDATES_CHANNEL_ID = 845784407846813696L; // #new-plugins
    public static final long PLUGIN_REQUESTS_CHANNEL_ID = 811275334342541353L; // #plugin-requests
    public static final long THEMES_CHANNEL_ID = 824357609778708580L; // #themes
    public static final long PLUGIN_DEVELOPMENT_CHANNEL_ID = 811261478875299840L; // #plugin-development
    public static final long PLUGIN_DEVELOPER_ROLE_ID = 811277662747623464L; // @plugin-developer
    public static final long SUPPORT_HELPER_ROLE_ID = 1397067198761144361L; // @support-helper

    /** Path of Aliucord folder */
    @SuppressWarnings("deprecation")
    public static final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Aliucord";
    /** Path of Plugin folder */
    public static final String PLUGINS_PATH = BASE_PATH + "/plugins";
    /** Path of Crashlog folder */
    public static final String CRASHLOGS_PATH = BASE_PATH + "/crashlogs";
    /** Path of Settings folder */
    public static final String SETTINGS_PATH = BASE_PATH + "/settings";

    public static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";
    public static final String NAMESPACE_APP = "http://schemas.android.com/apk/res-auto";

    /**
     * The release suffix of the currently running Discord. Some methods have this as their suffix and it changes with release, so in those
     * cases use {@code "someMethod$" + Constants.RELEASE_SUFFIX} with reflection so that it works on all releases.
     * <hr><br>
     * <h3>One of</h3>
     * <ul>
     *     <li>app_productionGoogleRelease</li>
     *     <li>app_productionBetaRelease</li>
     *     <li>app_productionCanaryRelease</li>
     * </ul>
     */
    public static final String RELEASE_SUFFIX;

    /** The version int of the currently running Discord, currently {@value BuildConfig#DISCORD_VERSION} */
    public static final int DISCORD_VERSION;

    static {
        int version;
        String suffix;

        try {
            //noinspection ConstantConditions
            version = (int) ReflectUtils.getField(
                    ReflectUtils.getField(StoreStream.Companion.access$getCollector$p(StoreStream.Companion), "clientVersion"),
                    "clientVersion"
            );

        } catch (Throwable e) {
            Main.logger.error("Failed to retrieve client version", e);
            version = BuildConfig.DISCORD_VERSION;
        }
        try {
            // Calculate the third digit of the number:
            //      101207 -> 2
            //      101107 -> 1
            //      101007 -> 0
            int release = (version / 100) % 10;
            suffix = new String[] { "app_productionGoogleRelease", "app_productionBetaRelease", "app_productionCanaryRelease" }[release];
        } catch (Throwable e) {
            Main.logger.error("Failed to determine discord release. Defaulting to beta", e);
            suffix = "app_productionBetaRelease";
        }

        DISCORD_VERSION = version;
        RELEASE_SUFFIX = suffix;
    }
}
