/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import com.discord.app.AppLog;

@SuppressWarnings("unused")
public class Logger {
    public String tag = "[Aliucord]";

    public Logger() {}
    public Logger(String mdl) { tag += " [" + mdl + "]"; }

    public void debug(String msg) { AppLog.g.d(getMsgWithTag(msg), null); }
    public void error(Throwable e) { error("Error:", e); }
    public void error(String msg, Throwable e) { AppLog.g.e(getMsgWithTag(msg), e, null); }
    public void info(String msg) { info(msg, null); }
    public void info(String msg, Throwable e) { AppLog.g.i(getMsgWithTag(msg), e); }
    public void verbose(String msg) { AppLog.g.v(getMsgWithTag(msg), null); }
    public void warn(String msg) { warn(msg, null); }
    public void warn(String msg, Throwable e) { AppLog.g.w(getMsgWithTag(msg), e); }

    private String getMsgWithTag(String msg) { return tag + " " + msg; }
}
