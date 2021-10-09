/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.Context;
import android.util.Log;

import com.discord.app.AppLog;

/** Logger that will log to both logcat and Discord's debug log */
@SuppressWarnings("unused")
public class Logger {
    /** Tag of the logger */
    public String tag = "[Aliucord]";

    private String getMsgWithTag(String msg) {
        return tag + " " + msg;
    }

    /** Creates a logger not associated with any module. Its tag will be simply [Aliucord] */
    public Logger() {}

    /**
     * Creates a logger for the specified module. Its tag will be [Aliucord] [MODULE]
     * @param module Name of the module
     */
    public Logger(String module) { tag += " [" + module + "]"; }

    /**
     * Logs a {@link Log#VERBOSE} message
     * @param msg Message to log
     */
    public void verbose(String msg) {
        AppLog.g.v(getMsgWithTag(msg), null);
    }

    /**
     * Logs a {@link Log#DEBUG} message
     * @param msg Message to log
     */
    public void debug(String msg) {
        AppLog.g.d(getMsgWithTag(msg), null);
    }

    /**
     * Logs a {@link Log#INFO} message
     * @param msg Message to log
     */
    public void info(String msg) {
        info(msg, null);
    }

    /**
     * Logs a {@link Log#INFO} message and prints the stacktrace of the exception
     * @param msg Message to log
     * @param throwable Exception to log
     */
    public void info(String msg, Throwable throwable) {
        AppLog.g.i(getMsgWithTag(msg), throwable);
    }

    /**
     * Logs a {@link Log#INFO} message, and shows it to the user as a toast
     * @param ctx Context
     * @param msg Message to log
     */
    public void info(Context ctx, String msg) {
        Utils.showToast(msg);
        info(msg, null);
    }

    /**
     * Logs a {@link Log#WARN} message
     * @param msg Message to log
     */
    public void warn(String msg) {
        warn(msg, null);
    }

    /**
     * Logs a {@link Log#WARN} message and prints the stacktrace of the exception
     * @param msg Message to log
     * @param throwable Exception to log
     */
    public void warn(String msg, Throwable throwable) {
        AppLog.g.w(getMsgWithTag(msg), throwable);
    }

    /**
     * Logs an exception
     * @param throwable Exception to log
     */
    public void error(Throwable throwable) {
        error("Error:", throwable);
    }

    /**
     * Logs a {@link Log#ERROR} message and prints the stacktrace of the exception
     * @param msg Message to log
     * @param throwable Exception to log
     */
    public void error(String msg, Throwable throwable) {
        AppLog.g.e(getMsgWithTag(msg), throwable, null);
    }

    /**
     * Logs an exception and shows the user a toast saying "Sorry, something went wrong. Please try again."
     * @param ctx Context
     * @param throwable Exception to log
     */
    public void error(Context ctx, Throwable throwable) {
        error(ctx, "Sorry, something went wrong. Please try again.", throwable);
    }

    /**
     * Logs a {@link Log#ERROR} message, and shows it to the user as a toast
     * @param ctx Context
     * @param msg Message to log
     */
    public void error(Context ctx, String msg) {
        error(ctx, msg, null);
    }

    /**
     * Logs a {@link Log#ERROR} message, shows it to the user as a toast and prints the stacktrace of the exception
     * @param ctx Context
     * @param msg Message to log
     */
    public void error(Context ctx, String msg, Throwable e) {
        Utils.showToast(msg, true);
        error(msg, e);
    }
}
