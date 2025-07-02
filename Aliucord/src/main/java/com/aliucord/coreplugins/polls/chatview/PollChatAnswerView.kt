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
import com.aliucord.coreplugins.polls.PollsStore.VotesSnapshot
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.discord.api.message.poll.MessagePollAnswer
import com.discord.utilities.accessibility.AccessibilityUtils
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.textprocessing.node.EmojiNode
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.views.CheckedSetting
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.lytefast.flexinput.R
import kotlin.math.roundToInt

internal class PollChatAnswerView private constructor(private val ctx: Context) : CheckedSetting(ctx, null) {
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var gutter: View
    private lateinit var checkmark: ImageView

    private val label get() = l.a()
    private val layout get() = l.b() as ConstraintLayout
    private val checkbox get() = l.c()
    private val subtext get() = l.f()

    private var isFirstSet = false

    companion object {
        fun build(ctx: Context, answer: MessagePollAnswer, isMultiselect: Boolean)
            = PollChatAnswerView(ctx).configure(answer, isMultiselect)
    }

    private fun configure(answer: MessagePollAnswer, isMultiselect: Boolean): PollChatAnswerView {
        removeAllViews()
        if (isMultiselect)
            f(ViewType.CHECK)
        else
            f(ViewType.RADIO)

        val p = DimenUtils.defaultPadding
        val p2 = DimenUtils.defaultPadding / 2
        setText(answer.pollMedia.text)

        layout.setPadding(0, p2, p, p2)
        subtext.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.d.uikit_textsize_small))
            setTextColor(ColorCompat.getThemedColor(ctx, R.b.primary_300))
            layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                topMargin = 0
            }
        }

        val fontScale = resources.configuration.fontScale
        layout.minHeight = DimenUtils.dpToPx(58f * fontScale)

        answer.pollMedia.emoji?.let { emoji ->
            val emojiView = SimpleDraweeSpanTextView(ctx).addTo(layout) {
                id = generateViewId()
                layoutParams = ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    marginStart = p
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                }
                EmojiNode.Companion!!.renderEmoji(this, emoji, true, p + p2)
            }

            label.layoutParams = (label.layoutParams as ConstraintLayout.LayoutParams).apply {
                marginStart = p2
                startToEnd = emojiView.id
            }
            subtext.layoutParams = (subtext.layoutParams as ConstraintLayout.LayoutParams).apply {
                marginStart = p2
                startToEnd = emojiView.id
            }
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

        updateState(PollChatView.State.VOTING, false)
        updateCount(VotesSnapshot.Detailed(), 1, false, PollChatView.State.VOTING)
        isFirstSet = false

        return this
    }

    fun updateCount(count: VotesSnapshot, totalCount: Int, isWinner: Boolean, state: PollChatView.State) {
        val progress = (count.count.toDouble() * 100 / totalCount).roundToInt()

        if (!isFirstSet) {
            isFirstSet = true
            progressIndicator.setProgress(progress)
        } else
            animateProgress(progress)

        val percent = "$progress%"
        val votes = "${count.count} vote${if (count.count != 1) "s" else ""}"
        val voteText = SpannableString("$percent  •  $votes").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, percent.length, 0)
        }
        subtext.text = voteText

        val typefaceStyle = if (isWinner || count.meVoted)
            Typeface.BOLD
        else
            Typeface.NORMAL
        gutter.visibility = if (isWinner) VISIBLE else GONE
        checkmark.visibility = if (count.meVoted) VISIBLE else GONE

        val color = if (isWinner)
            ColorCompat.getThemedColor(ctx, R.b.colorButtonPositiveBackground)
        else if (count.meVoted && state == PollChatView.State.VOTED)
            ColorCompat.getThemedColor(ctx, R.b.color_brand)
        else
            ColorCompat.getThemedColor(ctx, R.b.colorButtonSecondaryBackgroundActive)

        label.typeface = Typeface.create(label.typeface, typefaceStyle)
        subtext.typeface = Typeface.create(label.typeface, typefaceStyle)
        progressIndicator.setIndicatorColor(ColorUtils.setAlphaComponent(color, 0x50))
        DrawableCompat.setTint(checkmark.drawable, color)
    }

    fun updateState(state: PollChatView.State, shouldReanimate: Boolean) {
        when (state) {
            PollChatView.State.VOTING -> {
                layout.isClickable = true
                checkbox.visibility = VISIBLE
                subtext.visibility = GONE
                progressIndicator.visibility = GONE
            }
            PollChatView.State.SHOW_RESULT,
            PollChatView.State.CLOSED,
            PollChatView.State.FINALISED,
            PollChatView.State.VOTED -> {
                isChecked = false
                layout.isClickable = false
                checkbox.visibility = GONE
                subtext.visibility = VISIBLE
                if (shouldReanimate) {
                    val progress = progressIndicator.progress
                    progressIndicator.progress = 0
                    animateProgress(progress)
                }
                progressIndicator.visibility = VISIBLE
            }
        }
    }

    private fun animateProgress(target: Int) {
        if (AccessibilityUtils.INSTANCE.isReducedMotionEnabled)
            progressIndicator.setProgress(target)
        else
            progressIndicator.setProgress(target, true)
    }
}
