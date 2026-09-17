package com.aliucord.coreplugins.voice

internal object StreamQuality {
    // Past this zoom factor, request source quality for the watched stream
    private const val BOOST_SCALE = 1.5f
    private var currentUserId = 0L
    internal var onBoost: ((userId: Long, boost: Boolean) -> Unit)? = null

    // Called on every zoom change
    fun update(userId: Long, scale: Float) {
        if (userId == 0L) return

        val needed = scale > BOOST_SCALE

        if (needed == (userId == currentUserId)) return

        currentUserId = if (needed) userId else 0L
        onBoost?.invoke(userId, needed)
    }
}
