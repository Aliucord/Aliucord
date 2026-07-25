package com.aliucord.coreplugins.voice

import android.annotation.SuppressLint
import android.graphics.PointF
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.DisplayCutoutCompat
import com.aliucord.Utils
import com.aliucord.api.PatcherAPI
import com.aliucord.coreplugins.voice.ui.StreamOverlay
import com.aliucord.patcher.after
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.aliucord.patcher.component3
import com.aliucord.patcher.component4
import com.aliucord.patcher.component5
import com.discord.views.calls.VideoCallParticipantView
import com.discord.views.calls.VideoCallParticipantView.ParticipantData
import com.discord.widgets.voice.fullscreen.grid.VideoCallGridAdapter
import java.util.WeakHashMap
import kotlin.math.abs

// Pinch zoom + pan for the fullscreen stream screen
// Don't mess the stream itself but with the SurfaceView renderer
internal object StreamZoom {
    private const val MAX_SCALE = 12f  // todo: maybe make this a changeable setting?
    private val controllers = WeakHashMap<VideoCallParticipantView, Controller>()

    fun register(patcher: PatcherAPI) {
        val rendererId = Utils.getResId("participant_video_stream_renderer", "id")

        patcher.after<VideoCallParticipantView>(
            "c",
            ParticipantData::class.java,
            DisplayCutoutCompat::class.java,
            Boolean::class.javaPrimitiveType!!,
            VideoCallGridAdapter.CallUiInsets::class.java,
            Boolean::class.javaPrimitiveType!!,
        ) { (_, data: ParticipantData?, _: DisplayCutoutCompat, _: Boolean, insets: VideoCallGridAdapter.CallUiInsets?) ->
            controllers.getOrPut(this) {
                Controller(this, findViewById(rendererId)).also(::setOnTouchListener)
            }.configure(data, insets)
        }

        // Reset zoom and stop the info poll when renderer gets detached
        patcher.after<VideoCallParticipantView>("onDetachedFromWindow") {
            controllers[this]?.reset()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private class Controller(
        private val tile: VideoCallParticipantView,
        private val renderer: View?,
    ) : View.OnTouchListener {
        private val overlay = StreamOverlay(tile)
        private val touchSlop = ViewConfiguration.get(tile.context).scaledTouchSlop
        private var enabled = false
        private var dataId: String? = null
        private var userId = 0L
        private var insets: VideoCallGridAdapter.CallUiInsets? = null
        private var scale = 1f
        private val translation = PointF()
        private val down = PointF()
        private val last = PointF()
        private val focusPoint = PointF()
        private var panning = false
        private var suppressClick = false

        private val scaleDetector = ScaleGestureDetector(
            tile.context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val prev = scale
                    scale = (scale * detector.scaleFactor).coerceIn(1f, MAX_SCALE)

                    // keep the content under the pinch focus stationary while scaling
                    val factor = scale / prev
                    val fx = detector.focusX - tile.width / 2f
                    val fy = detector.focusY - tile.height / 2f

                    translation.set(
                        fx - (fx - translation.x) * factor,
                        fy - (fy - translation.y) * factor,
                    )

                    apply()
                    return true
                }
            }
        )

        fun configure(data: ParticipantData?, insets: VideoCallGridAdapter.CallUiInsets?) {
            this.insets = insets

            if (data?.id != dataId) {
                // Clears any quality override for the previous userId before swapping it, or the
                // previous streamer's max-quality override never gets cleared
                reset()
                dataId = data?.id
                // `b` = voiceParticipant
                // `user.id` = streamer whose quality we boost
                userId = data?.b?.user?.id ?: 0L
            }

            enabled = data != null
                && data.i  // isFocused
                && data.g == ParticipantData.Type.APPLICATION_STREAMING  // type

            if (enabled) overlay.startInfo(insets) else reset()
        }

        fun reset() {
            overlay.stopInfo()
            scale = 1f
            translation.set(0f, 0f)
            panning = false
            suppressClick = false
            apply()
        }

        private fun apply() {
            val view = renderer ?: return

            if (scale <= 1.001f) {
                scale = 1f
                translation.set(0f, 0f)
            }

            // don't pan past the scaled edges
            val maxX = (scale - 1f) * tile.width / 2f
            val maxY = (scale - 1f) * tile.height / 2f
            translation.set(
                translation.x.coerceIn(-maxX, maxX),
                translation.y.coerceIn(-maxY, maxY),
            )

            view.scaleX = scale
            view.scaleY = scale
            view.translationX = translation.x
            view.translationY = translation.y

            if (scale > 1f) overlay.showPill(scale, insets)

            // Ask the server for source quality once zoomed in, clear when reset
            StreamQuality.update(userId, scale)
        }

        // Average position of the active fingers; skipIndex handles ACTION_POINTER_UP,
        // where the lifting finger is still present in the event
        private fun focus(ev: MotionEvent, skipIndex: Int = -1): PointF {
            var sumX = 0f
            var sumY = 0f
            var count = 0

            for (i in 0 until ev.pointerCount) {
                if (i == skipIndex) continue

                sumX += ev.getX(i)
                sumY += ev.getY(i)
                count++
            }

            if (count > 0) {
                focusPoint.set(sumX / count, sumY / count)
            } else {
                focusPoint.set(ev.x, ev.y)
            }

            return focusPoint
        }

        override fun onTouch(v: View, ev: MotionEvent): Boolean {
            if (!enabled || renderer == null) return false

            scaleDetector.onTouchEvent(ev)

            val multiTouch = ev.pointerCount > 1

            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    down.set(ev.x, ev.y)
                    last.set(ev.x, ev.y)
                    panning = false
                    suppressClick = false

                    if (scale > 1f) v.parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    last.set(focus(ev))
                    suppressClick = true
                    v.cancelLongPress()
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    // focal point jumps when a finger lifts, prevents pan from snap
                    last.set(focus(ev, ev.actionIndex))
                }
                MotionEvent.ACTION_MOVE -> {
                    val c = focus(ev)

                    if (multiTouch || scale > 1f) {
                        if (!panning && (multiTouch || abs(c.x - down.x) + abs(c.y - down.y) > touchSlop)) {
                            panning = true
                            suppressClick = true
                            v.cancelLongPress()
                        }

                        if (panning) {
                            translation.offset(c.x - last.x, c.y - last.y)
                            apply()
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                    }

                    last.set(c)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    panning = false

                    if (suppressClick) {
                        suppressClick = false
                        return true
                    }
                }
            }

            return multiTouch || panning
        }
    }
}
