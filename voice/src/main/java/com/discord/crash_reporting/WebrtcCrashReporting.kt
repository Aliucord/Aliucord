package com.discord.crash_reporting

import android.util.Log

object WebrtcCrashReporting {
    @JvmStatic
    fun reportWebrtcException(th: Throwable): String {
        Log.e("Aliuvoice", "Caught webrtc exception", th)
        return th.toString()
    }
}
