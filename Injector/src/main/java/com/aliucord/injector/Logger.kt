/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2022 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.injector

import com.discord.app.AppLog

object Logger {
    private val TAG = "[$LOG_TAG] "

    fun d(msg: String) = AppLog.g.d(TAG + msg, null)
    fun w(msg: String) = AppLog.g.w(TAG + msg, null)
    fun e(msg: String, e: Throwable?) = AppLog.g.e(TAG + msg, e, null)
}
