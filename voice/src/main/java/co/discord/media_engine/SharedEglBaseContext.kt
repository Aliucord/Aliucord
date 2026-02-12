package co.discord.media_engine

import org.webrtc.EglBase
import org.webrtc.EglHelper

object SharedEglBaseContext {
    private var eglBase_: EglBase? = null

    @JvmStatic
    @Synchronized
    fun getEglContext(): EglBase.Context {
        val eglBaseContext: EglBase.Context?
        synchronized(SharedEglBaseContext::class.java) {
            if (eglBase_ == null) {
                eglBase_ = EglHelper.create()
            }
            eglBaseContext = eglBase_!!.eglBaseContext
        }
        return eglBaseContext!!
    }
}
