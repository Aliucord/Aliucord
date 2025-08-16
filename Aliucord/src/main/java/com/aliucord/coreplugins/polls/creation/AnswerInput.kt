package com.aliucord.coreplugins.polls.creation

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.Editable
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliucord.coreplugins.polls.creation.PollCreateScreen.Companion.asReactionEmoji
import com.aliucord.coreplugins.polls.creation.PollCreateScreen.Companion.createTextInput
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.discord.models.domain.emoji.Emoji
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.textprocessing.node.EmojiNode
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.lytefast.flexinput.R

internal class AnswerInput(private val ctx: Context): ConstraintLayout(ctx) {
    private companion object {
        val emojiButtonId = generateViewId()
        val textInputId = generateViewId()
        val deleteButtonId = generateViewId()

        val p = DimenUtils.defaultPadding
    }

    var onDelete: (() -> Unit)? = null
    var onOpenEmoji: (() -> Unit)? = null
    var onTextChange: ((Editable) -> Unit)? = null

    fun setText(text: CharSequence) {
        textInput.editText.setText(text)
    }

    fun setEmoji(emoji: Emoji?) {
        emojiButton.removeAllViews()
        if (emoji == null) {
            ImageView(ctx).addTo(emojiButton) {
                setImageResource(R.e.ic_emoji_24dp)
                val color = ColorCompat.getThemedColor(ctx, R.b.flexInputIconColor)
                drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }
        } else {
            SimpleDraweeSpanTextView(ctx).addTo(emojiButton) {
                layoutParams = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                EmojiNode.Companion!!.renderEmoji(this, emoji.asReactionEmoji(), true, DimenUtils.dpToPx(24))
            }
        }
    }

    fun setRemoveVisibility(visible: Boolean) {
        if (visible) {
            removeButton.visibility = VISIBLE
            textInput.layoutParams = (textInput.layoutParams as LayoutParams).apply {
                marginEnd = 0
            }
        } else {
            removeButton.visibility = GONE
            textInput.layoutParams = (textInput.layoutParams as LayoutParams).apply {
                marginEnd = p
            }
        }
    }

    private val emojiButton = FrameLayout(ctx).addTo(this) {
        ImageView(ctx).addTo(this) {
            setImageResource(R.e.ic_emoji_24dp)
            val color = ColorCompat.getThemedColor(ctx, R.b.flexInputIconColor)
            drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }

        id = emojiButtonId
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            topToTop = LayoutParams.PARENT_ID
            bottomToBottom = LayoutParams.PARENT_ID
            startToStart = LayoutParams.PARENT_ID
            endToStart = textInputId
            setPadding(p, 0, p, 0)
        }
        setOnClickListener {
            onOpenEmoji?.invoke()
        }
    }

    private val textInput =
        createTextInput(ctx, 55, "Type your answer") { onTextChange?.invoke(it) }
            .addTo(this) {
                id = textInputId
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT).apply {
                    topToTop = LayoutParams.PARENT_ID
                    bottomToBottom = LayoutParams.PARENT_ID
                    startToEnd = emojiButtonId
                    endToStart = deleteButtonId
                }
            }

    private val removeButton = ImageView(ctx).addTo(this) {
        id = deleteButtonId

        setImageResource(R.e.ic_delete_24dp)
        val color = ColorCompat.getThemedColor(ctx, R.b.flexInputIconColor)
        drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, 0).apply {
            topToTop = LayoutParams.PARENT_ID
            bottomToBottom = LayoutParams.PARENT_ID
            endToEnd = LayoutParams.PARENT_ID
            startToEnd = textInputId
            setPadding(p, 0, p, 0)
        }
        setOnClickListener { onDelete?.invoke() }
    }
}
