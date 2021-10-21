/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.injector;

import com.discord.app.AppLog;

public final class Logger {
    private static final String TAG = "[" + Injector.LOG_TAG + "] ";

    public static void d(String msg) { AppLog.g.d(TAG + msg, null); }
    public static void w(String msg) { AppLog.g.w(TAG + msg, null); }
    public static void e(String msg, Throwable e) { AppLog.g.e(TAG + msg, e, null); }
}
