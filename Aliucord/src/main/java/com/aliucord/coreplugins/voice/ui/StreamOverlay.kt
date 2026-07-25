package com.aliucord.coreplugins.voice.ui

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.aliucord.coreplugins.voice.model.StreamInfo
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.discord.utilities.color.ColorCompat
import com.discord.views.calls.VideoCallParticipantView
import com.discord.widgets.voice.fullscreen.grid.VideoCallGridAdapter.CallUiInsets
import com.lytefast.flexinput.R
import java.util.Locale

internal class StreamOverlay(
    private val tile: VideoCallParticipantView,
) {
    companion object {
        // Async fetch of the watched stream's stats
        var streamInfoProvider: ((cb: (StreamInfo?) -> Unit) -> Unit)? = null
    }

    private lateinit var rows: StatRows
    private lateinit var pill: TextView
    private var polling = false
    private var shownScale = -1f

    private val pillCard by lazy {
        PillCard(tile, PillCard.Corner.TOP_RIGHT).apply {
            autoHideAfter(1000)
            val p = DimenUtils.defaultPadding

            pill = TextView(context, null, 0, R.i.UiKit_TextView_Semibold).addTo(this) {
                gravity = Gravity.CENTER
                minWidth = 56.dp
                setPadding(p, p / 2, p, p / 2)
                setTextColor(ColorCompat.getThemedColor(context, R.b.colorOnSurface))
            }
        }
    }

    private val infoCard by lazy {
        PillCard(tile, PillCard.Corner.TOP_LEFT).apply {
            val p = DimenUtils.defaultPadding

            LinearLayout(context).addTo(this) {
                layoutParams = ViewGroup.LayoutParams(220.dp, ViewGroup.LayoutParams.WRAP_CONTENT)
                orientation = LinearLayout.VERTICAL
                setPadding(p, p / 2, p, p / 2)
                rows = StatRows(
                    codec = addRow("Video Codec"),
                    encoder = addRow("Encoder"),
                    decoder = addRow("Decoder"),
                    resolution = addRow("Resolution"),
                    fps = addRow("FPS"),
                    bitrate = addRow("Bitrate"),
                )
            }
        }
    }

    // Adds a "label  value" row, returns it (the value is right-aligned + weighted)
    private fun LinearLayout.addRow(label: String): Row {
        val value = TextView(context).apply {
            setTextColor(ColorCompat.getThemedColor(context, R.b.colorOnSurface))
            textSize = 12f
            gravity = Gravity.END
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { marginStart = 14.dp }
        }
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = 2.dp }
            addView(TextView(context).apply {
                text = "$label:"
                setTextColor(ColorCompat.getThemedColor(context, R.b.colorHeaderSecondary))
                textSize = 12f
            })
            addView(value)
        }

        addView(row)
        return Row(row, value)
    }

    private val pollInfo = object : Runnable {
        override fun run() {
            streamInfoProvider?.invoke { data ->
                tile.post { if (polling) applyInfo(data) }
            }

            infoCard.postDelayed(this, 2000)
        }
    }

    // Keep the last values on a null poll
    private fun applyInfo(d: StreamInfo?) {
        d ?: return

        rows.codec.set(d.codec)
        rows.encoder.set(d.encoder)
        rows.decoder.set(d.decoder)
        rows.resolution.set(d.resolution)
        rows.fps.set(d.fps)
        rows.bitrate.set(d.bitrate)
        infoCard.show()
    }

    fun showPill(scale: Float, insets: CallUiInsets?) {
        pillCard.applyInsets(insets)

        if (scale != shownScale) {
            shownScale = scale
            pill.text = String.format(Locale.ROOT, "%.1fx", scale)
        }

        pillCard.show()
    }

    fun startInfo(insets: CallUiInsets?) {
        infoCard.applyInsets(insets)
        if (polling) return

        polling = true
        infoCard.removeCallbacks(pollInfo)
        infoCard.post(pollInfo)
    }

    fun stopInfo() {
        if (!polling) return

        polling = false
        infoCard.removeCallbacks(pollInfo)
        infoCard.hide()
    }

    private class Row(
        private val container: View,
        private val value: TextView,
    ) {
        fun set(text: String?) {
            if (text == null) {
                container.visibility = View.GONE
            } else {
                value.text = text
                container.visibility = View.VISIBLE
            }
        }
    }

    private class StatRows(
        val codec: Row,
        val encoder: Row,
        val decoder: Row,
        val resolution: Row,
        val fps: Row,
        val bitrate: Row,
    )
}
