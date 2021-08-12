package com.discord.app;

import com.discord.utilities.logging.Logger;
import com.discord.utilities.mg_recycler.MGRecyclerDataPayload;

import java.util.Map;

@SuppressWarnings("unused")
public final class AppLog extends Logger {
    public static final class LoggedItem implements MGRecyclerDataPayload {
        /** message */
        public final String k;
        /** throwable */
        public final Throwable l;

        public LoggedItem(int i, String message, Throwable th) {
            k = message;
            l = th;
        }

        @Override
        public String getKey() { return null; }
        @Override
        public int getType() { return 0; }
    }

    /** INSTANCE */
    public static final AppLog g = new AppLog();

    /** debug */
    public void d(String msg, Throwable th) {}
    public void d(String tag, String msg, Throwable th) {}
    /** error */
    public void e(String msg, Throwable th, Map<String, String> metadata) {}
    public void e(String tag, String msg, Throwable th, Map<String, String> metadata) {}
    /** info */
    public void i(String msg, Throwable th) {}
    public void i(String tag, String msg, Throwable th) {}
    /** verbose */
    public void v(String msg, Throwable th) {}
    /** warn */
    public void w(String msg, Throwable th) {}
    public void w(String tag, String msg, Throwable th) {}
}
