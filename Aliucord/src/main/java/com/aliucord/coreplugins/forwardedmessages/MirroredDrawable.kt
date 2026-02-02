package com.aliucord.coreplugins.forwardedmessages

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat

class MirroredDrawable(base: Drawable) : Drawable() {
    private val wrapped: Drawable = DrawableCompat.wrap(base).mutate()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        wrapped.bounds = bounds
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        val cx = b.centerX().toFloat()
        val cy = b.centerY().toFloat()
        canvas.save()
        canvas.scale(-1f, 1f, cx, cy)
        wrapped.draw(canvas)
        canvas.restore()
    }

    override fun getIntrinsicWidth(): Int = wrapped.intrinsicWidth
    override fun getIntrinsicHeight(): Int = wrapped.intrinsicHeight
    override fun setAlpha(alpha: Int) = wrapped.setAlpha(alpha)
    override fun setColorFilter(colorFilter: ColorFilter?) = wrapped.setColorFilter(colorFilter)
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = wrapped.opacity

    override fun setTintList(tint: ColorStateList?) {
        DrawableCompat.setTintList(wrapped, tint)
    }

    override fun setTint(tint: Int) {
        DrawableCompat.setTint(wrapped, tint)
    }

    override fun setTintMode(mode: android.graphics.PorterDuff.Mode?) {
        if (mode != null) DrawableCompat.setTintMode(wrapped, mode) else DrawableCompat.setTintMode(wrapped, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    override fun setAutoMirrored(mirrored: Boolean) = wrapped.setAutoMirrored(mirrored)
}
