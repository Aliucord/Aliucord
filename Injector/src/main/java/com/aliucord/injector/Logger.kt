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
import com.discord.utilities.logging.LoggingProvider

/**
 * A quick wrapper over Discord's internal logger that is viewable within the app.
 */
internal object Logger {
    /**
     * Sets up Discord's internal [AppLog] to allow logging.
     * This logic is extracted from [com.discord.app.App.onCreate].
     */
    fun init() {
        AppLog.a = 0 // AppLog#minLoggingPriority
        AppLog.b = true // AppLog#initCalled
        LoggingProvider.INSTANCE.init(AppLog.g)
    }

    private fun formatLog(msg: String) = "[${BuildConfig.TAG}] $msg"
    fun d(msg: String, e: Throwable? = null) = AppLog.g.d(formatLog(msg), e)
    fun i(msg: String, e: Throwable? = null) = AppLog.g.i(formatLog(msg), e)
    fun w(msg: String, e: Throwable? = null) = AppLog.g.w(formatLog(msg), e)
    fun e(msg: String, e: Throwable? = null) = AppLog.g.e(formatLog(msg), e, null)

    fun errorToast(ctx: Context, msg: String, e: Throwable? = null) {
        e(msg, e)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
        }
    }
}
