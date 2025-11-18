package com.discord.crash_reporting

import com.aliucord.injector.Logger

object WebrtcCrashReporting {
    @JvmStatic
    fun reportWebrtcException(th: Throwable): String {
        Logger.e("Caught webrtc exception", th)
        return th.toString()
    }
}
