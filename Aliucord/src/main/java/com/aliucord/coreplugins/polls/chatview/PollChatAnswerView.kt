package com.aliucord.coreplugins.polls.chatview

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import com.aliucord.coreplugins.polls.details.PollDetailsScreen
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.checkbox
import com.aliucord.utils.ViewUtils.label
import com.aliucord.utils.ViewUtils.layout
import com.aliucord.utils.ViewUtils.subtext
import com.discord.utilities.accessibility.AccessibilityUtils
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.textprocessing.node.EmojiNode
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.views.CheckedSetting
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.lytefast.flexinput.R
import kotlin.math.roundToInt

internal class PollChatAnswerView private constructor(private val ctx: Context) : CheckedSetting(ctx, null) {
    private lateinit var emojiView: SimpleDraweeSpanTextView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var gutter: View
    private lateinit var checkmark: ImageView

    private var answerId = -1
    private var isFirstSet = false
    private lateinit var defaultOnClickListener: OnClickListener
    private lateinit var detailsOnClickListener: OnClickListener

    companion object {
        fun build(
            ctx: Context,
            channelId: Long,
            messageId: Long,
            answerId: Int,
            isMultiselect: Boolean,
            onClickListener: PollChatAnswerView.() -> Unit
        ): PollChatAnswerView {
            return PollChatAnswerView(ctx).apply {
                defaultOnClickListener = OnClickListener { onClickListener() }
                detailsOnClickListener = OnClickListener { PollDetailsScreen.launch(ctx, channelId, messageId, answerId) }
                configure(answerId, isMultiselect)
            }
        }
    }

    private fun configure(answerId: Int, isMultiselect: Boolean): PollChatAnswerView {
        this.answerId = answerId
        removeAllViews()
        if (isMultiselect) {
            f(ViewType.CHECK)
        } else {
            f(ViewType.RADIO)
        }

        val p = DimenUtils.defaultPadding
        val p2 = DimenUtils.defaultPadding / 2

        layout.setPadding(p, p2, p, p2)
        subtext.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.d.uikit_textsize_small))
            setTextColor(ColorCompat.getThemedColor(ctx, R.b.primary_300))
            layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                topMargin = 0
            }
        }

        val fontScale = resources.configuration.fontScale
        layout.minHeight = DimenUtils.dpToPx(58f * fontScale)

        emojiView = SimpleDraweeSpanTextView(ctx).addTo(layout) {
            id = generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }

        label.layoutParams = (label.layoutParams as ConstraintLayout.LayoutParams).apply {
            marginStart = p2
            startToEnd = emojiView.id
        }
        subtext.layoutParams = (subtext.layoutParams as ConstraintLayout.LayoutParams).apply {
            marginStart = p2
            startToEnd = emojiView.id
        }

        // TODO: progressIndicator and gutter won't span the whole height for multi-line text
        progressIndicator = LinearProgressIndicator(ctx).addTo(this, 0) {
            trackColor = Color.TRANSPARENT
            trackThickness = layout.minHeight
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        gutter = View(ctx).addTo(this) {
            setBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorButtonPositiveBackground))
            minimumHeight = layout.minHeight
            layoutParams = LayoutParams(DimenUtils.dpToPx(2), LayoutParams.WRAP_CONTENT).apply {
                addRule(ALIGN_PARENT_START)
            }
        }

        checkmark = ImageView(ctx).addTo(layout) {
            visibility = GONE
            setImageResource(R.e.ic_check_circle_24dp)
            drawable.mutate()
            setPadding(0, 0, p / 4, 0)
            layoutParams = ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }

        isFirstSet = false
        return this
    }

    fun configureUI(model: PollChatView.Model, isStateTransition: Boolean) {
        val answer = model.answers.find { it.id == answerId }
            ?: return
        val state = model.state

        setText(answer.text)
        val emoji = answer.emoji
        if (emoji != null) {
            emojiView.visibility = VISIBLE
            EmojiNode.Companion!!.renderEmoji(emojiView, answer.emoji, true, 24.dp)
        } else {
            emojiView.visibility = GONE
        }

        if (state == PollChatView.State.VOTING) {
            layout.setOnClickListener(defaultOnClickListener)
            layout.isClickable = !model.submittingVote
            checkbox.visibility = VISIBLE
            subtext.visibility = GONE
            progressIndicator.visibility = GONE
        } else {
            isChecked = false
            layout.setOnClickListener(detailsOnClickListener)
            checkbox.visibility = GONE
            subtext.visibility = VISIBLE
            progressIndicator.visibility = VISIBLE
        }

        val progress = (answer.votes.toDouble() * 100 / model.totalVotes.coerceAtLeast(1)).roundToInt()
        val isWinner = model.state == PollChatView.State.CLOSED && answer.votes == model.answers.maxOf { it.votes }.coerceAtLeast(1)

        val percent = "$progress%"
        val votes = "${answer.votes} vote${if (answer.votes != 1) "s" else ""}"
        val voteText = SpannableString("$percent  •  $votes").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, percent.length, 0)
        }
        subtext.text = voteText

        val typefaceStyle = if (isWinner || answer.meVoted) {
            Typeface.BOLD
        } else {
            Typeface.NORMAL
        }
        gutter.visibility = if (isWinner) VISIBLE else GONE
        checkmark.visibility = if (answer.meVoted) VISIBLE else GONE
        isChecked = answer.checked

        val colorId = when {
            isWinner -> R.b.colorButtonPositiveBackground
            answer.meVoted && state == PollChatView.State.VOTED -> R.b.color_brand
            else -> R.b.colorButtonSecondaryBackgroundActive
        }
        val color = ColorCompat.getThemedColor(ctx, colorId)

        label.typeface = Typeface.create(label.typeface, typefaceStyle)
        subtext.typeface = Typeface.create(label.typeface, typefaceStyle)
        progressIndicator.setIndicatorColor(ColorUtils.setAlphaComponent(color, 0x50))
        DrawableCompat.setTint(checkmark.drawable, color)

        if (!isFirstSet) {
            isFirstSet = true
            progressIndicator.setProgressCompat(progress, false)
            checkbox.jumpDrawablesToCurrentState()
        } else {
            if (isStateTransition) {
                progressIndicator.progress = 0
            }
            progressIndicator.setProgressCompat(progress, !AccessibilityUtils.INSTANCE.isReducedMotionEnabled)
        }
    }
}
