package com.aliucord.coreplugins.voice.ui

import android.annotation.SuppressLint
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.DimenUtils.dp
import com.discord.utilities.color.ColorCompat
import com.discord.views.calls.VideoCallParticipantView
import com.discord.widgets.voice.fullscreen.grid.VideoCallGridAdapter.CallUiInsets
import com.google.android.material.card.MaterialCardView
import com.lytefast.flexinput.R

@SuppressLint("ViewConstructor")
internal class PillCard(
    tile: VideoCallParticipantView,
    private val corner: Corner,
) : MaterialCardView(tile.context) {
    companion object {
        private const val ALPHA = 128
        private const val FADE_MS = 300L
    }

    enum class Corner { TOP_LEFT, TOP_RIGHT }
    private var hideDelay = 0L
    private val autoHide = Runnable { hide() }

    init {
        id = generateViewId()  // ConstraintLayout, always new id
        radius = DimenUtils.defaultCardRadius.toFloat()
        cardElevation = 0f
        isClickable = false
        isFocusable = false
        alpha = 0f
        setCardBackgroundColor(ColorUtils.setAlphaComponent(ColorCompat.getThemedColor(context, R.b.colorSurface), ALPHA))

        tile.addView(this, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            topMargin = 8.dp
            if (corner == Corner.TOP_LEFT) {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                marginStart = 8.dp
            } else {
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                marginEnd = 8.dp
            }
        })
    }

    // Auto-hide this, ms = 0 keeps it up until hide()
    fun autoHideAfter(ms: Long) {
        hideDelay = ms
    }

    fun applyInsets(insets: CallUiInsets?) {
        translationX = if (corner == Corner.TOP_LEFT) {
            insets?.left?.toFloat() ?: 0f
        } else {
            -(insets?.right?.toFloat() ?: 0f)
        }
        translationY = insets?.top?.toFloat() ?: 0f
    }

    fun show() {
        animate().cancel()
        alpha = 1f
        removeCallbacks(autoHide)

        if (hideDelay > 0L) postDelayed(autoHide, hideDelay)
    }

    fun hide() {
        removeCallbacks(autoHide)
        animate().alpha(0f).setDuration(FADE_MS).start()
    }
}
