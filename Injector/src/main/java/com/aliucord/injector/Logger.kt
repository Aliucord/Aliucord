/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2022 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.injector

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.discord.app.AppLog

/**
 * A quick wrapper over Discord's internal logger that is viewable within the app.
 * Calls to this logger will only function after the main activity has been initialized within Discord.
 */
internal object Logger {
    fun d(msg: String, e: Throwable? = null) = AppLog.g.d("[${BuildConfig.TAG}] $msg", e)
    fun i(msg: String, e: Throwable? = null) = AppLog.g.i("[${BuildConfig.TAG}] $msg", e)
    fun w(msg: String, e: Throwable? = null) = AppLog.g.w("[${BuildConfig.TAG}] $msg", e)
    fun e(msg: String, e: Throwable? = null) = AppLog.g.e("[${BuildConfig.TAG}] $msg", e, null)

    fun errorToast(ctx: Context, msg: String, e: Throwable? = null) {
        e(msg, e)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
        }
    }
}
