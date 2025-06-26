package com.aliucord.coreplugins.polls.details

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils
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

        @SuppressLint("SetTextI18n")
        override fun onConfigure(position: Int, data: AnswerItem) {
            super.onConfigure(position, data)

            EmojiNode.Companion!!.renderEmoji(emojiView, data.answer.pollMedia.emoji, true, DimenUtils.dpToPx(20))
            textView.text = "${data.answer.pollMedia.text} (${data.voteCount})"
            selectedIndicatorView.visibility = if (data.isSelected) View.VISIBLE else View.INVISIBLE
            containerView.setOnClickListener {
                adapter.onAnswerSelected(data.answer.answerId!!)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MGRecyclerViewHolder<PollDetailsAnswersAdapter, AnswerItem> {
        if (viewType != 0)
            throw invalidViewTypeException(viewType)
        return AnswerViewHolder(this)
    }
}
