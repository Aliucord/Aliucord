package co.discord.media_engine

import org.webrtc.EglBase

/**
 * Fork-owned: binds to this module's own [EglBase] so the returned [EglBase.Context] matches the
 * one [com.discord.native.engine.NativeEngine.nativeCreateInstance] expects. Must be source here
 * (not resolved from the Discord dependency) or its `org.webrtc.EglBase` would shadow ours.
 */
object SharedEglBaseContext {
    private var eglBase: EglBase? = null

    @JvmStatic
    @Synchronized
    fun getEglContext(): EglBase.Context {
        if (eglBase == null) {
            eglBase = EglBase.create()
        }
        return eglBase!!.eglBaseContext
    }
}
