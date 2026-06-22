package co.discord.media_engine

import android.os.Handler
import android.os.HandlerThread

class TransportCoalescer<T : Any>(
    name: String,
    private val merge: (T, T) -> T,
    private val toJson: (T) -> String,
    private val isAlive: () -> Boolean,
    private val send: (String) -> Unit,
) {
    private val thread = HandlerThread(name).apply { start() }
    private val handler = Handler(thread.looper)
    private val lock = Any()
    private var pending: T? = null
    private var posted = false
    private var lastJson: String? = null

    fun submit(options: T) {
        val post: Boolean
        synchronized(lock) {
            pending = pending?.let { merge(it, options) } ?: options
            post = !posted
            if (post) posted = true
        }
        if (!post) return
        handler.post {
            val merged: T?
            synchronized(lock) {
                posted = false
                merged = pending
                pending = null
            }
            if (!isAlive()) return@post
            val o = merged ?: return@post
            val json = toJson(o)
            if (json != lastJson) {
                lastJson = json
                send(json)
            }
        }
    }

    fun release() {
        thread.quitSafely()
    }
}
