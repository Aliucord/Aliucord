/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.os.Environment;

import com.discord.stores.StoreClientVersion;
import com.discord.stores.StoreStream;

@SuppressWarnings("unused")
public final class Constants {
    // Font resource ids, they're not defined by any generated package but they seem to be constant so i made this class.
    public static final class Fonts {
        private static final int base = 0x7f090000;

        public static final int GINTO_BOLD = base;
        public static final int GINTO_MEDIUM = base + 1;
        public static final int GINTO_REGULAR = base + 2;
        public static final int ROBOTO_MEDIUM_NUMBERS = base + 3;
        public static final int SOURCECODEPRO_SEMIBOLD = base + 4;
        public static final int WHITNEY_BOLD = base + 5;
        public static final int WHITNEY_MEDIUM = base + 6;
        public static final int WHITNEY_SEMIBOLD = base + 7;
    }

    public static final String ALIUCORD_SUPPORT = "EsNDvBaHVU";

    public static final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Aliucord";

    public static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";
    public static final String NAMESPACE_APP = "http://schemas.android.com/apk/res-auto";

    public static final int DISCORD_VERSION;

    static {
        int version = 0;
        try {
            //noinspection AccessStaticViaInstance
            version = (int) Utils.getPrivateField(
                    StoreClientVersion.class,
                    StoreStream.Companion.access$getCollector$p(StoreStream.Companion).getClientVersion$app_productionBetaRelease(),
                    "clientVersion"
            );
        } catch (Throwable e) { Main.logger.error(e); }
        DISCORD_VERSION = version;
    }
}
