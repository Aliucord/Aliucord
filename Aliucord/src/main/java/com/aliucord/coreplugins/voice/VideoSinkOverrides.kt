package com.aliucord.coreplugins.voice

import com.discord.rtcconnection.EncodeQuality
import java.util.concurrent.ConcurrentHashMap

internal object VideoSinkOverrides {
    private val userSsrc = ConcurrentHashMap<Long, Int>()  // userId -> video ssrc (op 12)
    private val overrides = ConcurrentHashMap<Int, Int>()  // ssrc -> forced EncodeQuality.value
    @Volatile
    private var lastBase: Map<String, Any?> = mapOf("any" to EncodeQuality.Hundred.value)
    @Volatile
    private var resending = false

    fun learnSsrc(userId: Long, videoSsrc: Int) {
        if (videoSsrc != 0) userSsrc[userId] = videoSsrc
    }

    fun clearAll() {
        userSsrc.clear()
        overrides.clear()
    }

    // null quality clears the override
    fun setUserQuality(userId: Long, quality: EncodeQuality?): Boolean {
        val ssrc = userSsrc[userId] ?: return false
        val prev = if (quality == null) overrides.remove(ssrc) else overrides.put(ssrc, quality.value)
        return prev != quality?.value
    }

    fun onOutgoing(base: Map<String, Any?>): Map<String, Any?> {
        if (resending) return base
        lastBase = base
        return merge(base)
    }

    // Run a manual resend with the current overrides
    fun resend(send: (Map<String, Any?>) -> Unit) {
        resending = true
        try {
            send(merge(lastBase))
        } finally {
            resending = false
        }
    }

    private fun merge(base: Map<String, Any?>): Map<String, Any?> {
        if (overrides.isEmpty()) return base
        val out = LinkedHashMap<String, Any?>(base)
        overrides.forEach { (ssrc, value) -> out[ssrc.toString()] = value }
        return out
    }
}
