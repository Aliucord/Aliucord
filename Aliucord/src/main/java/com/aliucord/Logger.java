/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.Context;

import com.discord.app.AppLog;

@SuppressWarnings("unused")
public class Logger {
    public String tag = "[Aliucord]";

    public Logger() {}
    public Logger(String module) { tag += " [" + module + "]"; }

    public void debug(String msg) { AppLog.g.d(getMsgWithTag(msg), null); }
    public void error(Throwable e) { error("Error:", e); }
    public void error(String msg, Throwable e) { AppLog.g.e(getMsgWithTag(msg), e, null); }
    public void error(Context ctx, Throwable e) { error(ctx, "Sorry, something went wrong. Please try again", e); }
    public void error(Context ctx, String msg) { error(ctx, msg, null); }
        public void error(Context ctx, String msg, Throwable e) {
        Utils.showToast(ctx, msg);
        error(msg, e);
    }
    public void info(String msg) { info(msg, null); }
    public void info(String msg, Throwable e) { AppLog.g.i(getMsgWithTag(msg), e); }
    public void info(Context ctx, String msg) {
        Utils.showToast(ctx, msg);
        info(msg, null);
    }
    public void verbose(String msg) { AppLog.g.v(getMsgWithTag(msg), null); }
    public void warn(String msg) { warn(msg, null); }
    public void warn(String msg, Throwable e) { AppLog.g.w(getMsgWithTag(msg), e); }

    private String getMsgWithTag(String msg) { return tag + " " + msg; }
}
