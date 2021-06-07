package com.discord.app;

import com.discord.utilities.logging.Logger;

import java.util.Map;

@SuppressWarnings("unused")
public class AppLog extends Logger {
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
