package com.aliucord;

import android.os.Environment;

import com.discord.stores.StoreClientVersion;
import com.discord.stores.StoreStream;

@SuppressWarnings("unused")
public class Constants {
    // Font resource ids, they're not defined by any generated package but they seem to be constant so i made this class.
    public static class Fonts {
        private static final int base = 0x7f090000;

        public static int sourcecodepro_semibold = base;
        public static int whitney_bold = base + 1;
        public static int whitney_medium = base + 2;
        public static int whitney_semibold = base + 3;
    }

    public static String ALIUCORD_SUPPORT = "EsNDvBaHVU";

    public static String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Aliucord";

    public static String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";
    public static String NAMESPACE_APP = "http://schemas.android.com/apk/res-auto";

    public static int DISCORD_VERSION;

    static {
        try {
            DISCORD_VERSION = (int) Utils.getPrivateField(
                    StoreClientVersion.class,
                    StoreStream.access$getCollector$cp().getClientVersion$app_productionGoogleRelease(),
                    "clientVersion"
            );
        } catch (Throwable e) { Main.logger.error(e); }
    }
}
