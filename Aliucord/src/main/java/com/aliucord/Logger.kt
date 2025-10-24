/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord

import android.content.Context
import android.util.Log
import com.aliucord.Utils.showToast
import com.discord.app.AppLog

/**
 * Logger that will log to both logcat and Discord's debug log
 * @param module Name of the module
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class Logger(var module: String = "Aliucord") {
    private fun format(msg: String) = "[$module] $msg"

    /**
     * Logs a [Log.VERBOSE] message
     * @param msg Message to log
     */
    fun verbose(msg: String) = AppLog.g.v(format(msg), null)

    /**
     * Logs a [Log.DEBUG] message
     * @param msg Message to log
     */
    fun debug(msg: String) = AppLog.g.d(format(msg), null)

    /**
     * Logs a [Log.INFO] message and prints the stacktrace of the exception
     * @param msg Message to log
     * @param throwable Exception to log
     */
    @JvmOverloads
    fun info(msg: String, throwable: Throwable? = null) =
        AppLog.g.i(format(msg), throwable)

    /**
     * Logs a [Log.INFO] message, and shows it to the user as a toast
     * @param msg Message to log
     */
    @Deprecated("Use infoToast(msg) instead", ReplaceWith("infoToast(msg)"))
    fun info(ctx: Context?, msg: String) =
        infoToast(msg)

    /**
     * Logs a [Log.INFO] message, and shows it to the user as a toast
     * @param msg Message to log
     */
    fun infoToast(msg: String) {
        showToast(msg)
        info(msg, null)
    }

    /**
     * Logs a [Log.WARN] message and prints the stacktrace of the exception
     * @param msg Message to log
     * @param throwable Exception to log
     */
    @JvmOverloads
    fun warn(msg: String, throwable: Throwable? = null) =
        AppLog.g.w(format(msg), throwable)

    /**
     * Logs an exception
     * @param throwable Exception to log
     */
    fun error(throwable: Throwable?) =
        error("Error:", throwable)

    /**
     * Logs a [Log.ERROR] message and prints the stacktrace of the exception
     * @param msg Message to log
     * @param throwable Exception to log
     */
    fun error(msg: String, throwable: Throwable?) =
        AppLog.g.e(format(msg), throwable, null)

    /**
     * Logs an exception and shows the user a toast saying "Sorry, something went wrong. Please try again."
     * @param throwable Exception to log
     */
    fun errorToast(throwable: Throwable?) =
        errorToast("Sorry, something went wrong. Please try again.", throwable)

    /**
     * Logs an exception and shows the user a toast saying "Sorry, something went wrong. Please try again."
     * @param throwable Exception to log
     */
    @Deprecated("Use errorToast(throwable) instead", ReplaceWith("errorToast(throwable)"))
    fun error(ctx: Context?, throwable: Throwable?) =
        errorToast("Sorry, something went wrong. Please try again.", throwable)

    /**
     * Logs a [Log.ERROR] message, shows it to the user as a toast and prints the stacktrace of the exception
     * @param msg Message to log
     * @param throwable Exception to log
     */
    @JvmOverloads
    fun errorToast(msg: String, throwable: Throwable? = null) {
        showToast(msg, true)
        error(msg, throwable)
    }

    /**
     * Logs a [Log.ERROR] message, shows it to the user as a toast and prints the stacktrace of the exception
     * @param msg Message to log
     * @param throwable Exception to log
     */
    @JvmOverloads
    @Deprecated("Use errorToast(msg, throwable) instead", ReplaceWith("errorToast(msg, throwable)"))
    fun errorToast(ctx: Context?, msg: String, throwable: Throwable? = null) {
        showToast(msg, true)
        error(msg, throwable)
    }
}
