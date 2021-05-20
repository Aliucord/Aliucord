/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.os.Environment;

import com.discord.stores.StoreClientVersion;
import com.discord.stores.StoreStream;

@SuppressWarnings("unused")
public class Constants {
    // Font resource ids, they're not defined by any generated package but they seem to be constant so i made this class.
    public static class Fonts {
        private static final int base = 0x7f090000;

        public static int ginto_bold = base;
        public static int ginto_medium = base + 1;
        public static int ginto_regular = base + 2;
        public static int roboto_medium_numbers = base + 3;
        public static int sourcecodepro_semibold = base + 4;
        public static int whitney_bold = base + 5;
        public static int whitney_medium = base + 6;
        public static int whitney_semibold = base + 7;
    }

    public static String ALIUCORD_SUPPORT = "EsNDvBaHVU";

    public static String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Aliucord";

    public static String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";
    public static String NAMESPACE_APP = "http://schemas.android.com/apk/res-auto";

    public static int DISCORD_VERSION;

    static {
        try {
            //noinspection AccessStaticViaInstance
            DISCORD_VERSION = (int) Utils.getPrivateField(
                    StoreClientVersion.class,
                    StoreStream.Companion.access$getCollector$p(StoreStream.Companion).getClientVersion$app_productionBetaRelease(),
                    "clientVersion"
            );
        } catch (Throwable e) { Main.logger.error(e); }
    }
}
