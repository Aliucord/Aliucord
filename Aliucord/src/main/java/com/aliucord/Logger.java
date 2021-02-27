package com.aliucord;

import com.discord.app.AppLog;

@SuppressWarnings("unused")
public class Logger {
    public String tag = "[Aliucord]";

    public Logger() {}
    public Logger(String mdl) { tag += " [" + mdl + "]"; }

    public void debug(String msg) { AppLog.e.d(getMsgWithTag(msg), null); }
    public void error(Throwable e) { error("Error:", e); }
    public void error(String msg, Throwable e) { AppLog.e.e(getMsgWithTag(msg), e, null); }
    public void info(String msg) { info(msg, null); }
    public void info(String msg, Throwable e) { AppLog.e.i(getMsgWithTag(msg), e); }
    public void verbose(String msg) { AppLog.e.v(getMsgWithTag(msg), null); }

    private String getMsgWithTag(String msg) { return tag + " " + msg; }
}
