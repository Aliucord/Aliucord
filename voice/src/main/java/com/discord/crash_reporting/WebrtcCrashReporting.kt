package com.discord.crash_reporting

import android.util.Log

// Error handler called by native code
@Suppress("unused")
object WebrtcCrashReporting {
    @JvmStatic
    fun reportWebrtcException(th: Throwable): String {
        Log.e("Sunflower", "Caught webrtc exception", th)
        return th.toString()
    }
}
