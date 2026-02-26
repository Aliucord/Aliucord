package org.webrtc

object EglBaseFactory {
    fun create(context: EglBase.Context?, iArr: IntArray): EglBase {
        if (context == null) {
            // Originally this will check and use EglBase10 if SDK < 18 (4.3 Jelly Bean)
            // ... we are not going to bother
            return EglBase14Impl(null, iArr)
        }
        if (context is EglBase14.Context) {
            return EglBase14Impl(context.rawContext, iArr)
        }
        if (context is EglBase10.Context) {
            return EglBase10Impl(context.rawContext, iArr)
        }
        throw IllegalArgumentException("Unrecognized Context")
    }
}
