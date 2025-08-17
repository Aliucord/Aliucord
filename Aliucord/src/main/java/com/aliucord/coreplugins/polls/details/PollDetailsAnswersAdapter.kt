package com.aliucord.coreplugins.polls.details

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils.dp
import com.discord.api.message.poll.MessagePollAnswer
import com.discord.utilities.mg_recycler.*
import com.discord.utilities.textprocessing.node.EmojiNode
import com.discord.utilities.view.text.SimpleDraweeSpanTextView

internal class PollDetailsAnswersAdapter(
    recyclerView: RecyclerView,
    val onAnswerSelected: (answerId: Int) -> Unit
) : MGRecyclerAdapterSimple<PollDetailsAnswersAdapter.AnswerItem>(recyclerView) {
    class AnswerItem(
        val answer: MessagePollAnswer,
        val voteCount: Int,
        val isSelected: Boolean,
    ) : MGRecyclerDataPayload {
        override fun getKey() = answer.answerId.toString()
        override fun getType() = 0
    }

    private class AnswerViewHolder(adapter: PollDetailsAnswersAdapter)
        : MGRecyclerViewHolder<PollDetailsAnswersAdapter, AnswerItem>(Utils.getResId("widget_manage_reactions_emoji", "layout"), adapter) {

        val containerView: LinearLayout = itemView.findViewById(Utils.getResId("manage_reactions_emoji_container", "id"))
        val emojiView: SimpleDraweeSpanTextView = itemView.findViewById(Utils.getResId("manage_reactions_emoji_emoji_textview", "id"))
        val textView: TextView = itemView.findViewById(Utils.getResId("manage_reactions_emoji_counter", "id"))
        val selectedIndicatorView: View = itemView.findViewById(Utils.getResId("manage_reactions_emoji_selected_indicator", "id"))

        init {
            emojiView.layoutParams = (emojiView.layoutParams as LinearLayout.LayoutParams).apply {
                marginStart = 8.dp
            }
            textView.layoutParams = (textView.layoutParams as LinearLayout.LayoutParams).apply {
                marginEnd = 8.dp
            }
        }

        override fun onConfigure(position: Int, data: AnswerItem) {
            super.onConfigure(position, data)

            val emoji = data.answer.pollMedia.emoji
            if (emoji != null) {
                emojiView.visibility = View.VISIBLE
                EmojiNode.Companion!!.renderEmoji(emojiView, emoji, true, 20.dp)
            } else {
                emojiView.visibility = View.GONE
            }

            textView.text = "${data.answer.pollMedia.text} (${data.voteCount})"
            selectedIndicatorView.visibility = if (data.isSelected) View.VISIBLE else View.INVISIBLE
            itemView.setOnClickListener {
                adapter.onAnswerSelected(data.answer.answerId!!)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MGRecyclerViewHolder<PollDetailsAnswersAdapter, AnswerItem> {
        if (viewType != 0) {
            throw invalidViewTypeException(viewType)
        }
        return AnswerViewHolder(this)
    }
}
