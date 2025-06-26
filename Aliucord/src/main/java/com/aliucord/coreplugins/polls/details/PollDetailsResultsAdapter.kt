@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls.details

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.coreplugins.polls.PollsStore
import com.discord.utilities.drawable.DrawableCompat
import com.discord.utilities.mg_recycler.MGRecyclerDataPayload
import com.discord.utilities.mg_recycler.MGRecyclerViewHolder
import com.discord.widgets.chat.managereactions.ManageReactionsResultsAdapter
import com.lytefast.flexinput.R

internal class PollDetailsResultsAdapter(recyclerView: RecyclerView) : ManageReactionsResultsAdapter(recyclerView) {
    class EmptyItem : MGRecyclerDataPayload {
        override fun getKey() = "3"
        override fun getType() = 3
    }

    @SuppressLint("SetTextI18n")
    private class EmptyViewHolder(adapter: ManageReactionsResultsAdapter)
        : MGRecyclerViewHolder<ManageReactionsResultsAdapter, MGRecyclerDataPayload>(Utils.getResId("widget_manage_reactions_result_error", "layout"), adapter) {

        val imageView: ImageView = itemView.findViewById(Utils.getResId("manage_reactions_result_error_img", "id"))
        val textView: TextView = itemView.findViewById(Utils.getResId("manage_reactions_result_error_text", "id"))

        init {
            imageView.setImageResource(DrawableCompat.getThemedDrawableRes(itemView, R.b.theme_friends_no_friends))
            textView.text = "There are no votes for this answer"
        }
    }

    class ErrorItem(val channelId: Long, val messageId: Long, val answerId: Int) : MGRecyclerDataPayload {
        override fun getKey() = "4"
        override fun getType() = 4
    }

    @SuppressLint("SetTextI18n")
    private class ErrorViewHolder(adapter: ManageReactionsResultsAdapter)
        : MGRecyclerViewHolder<ManageReactionsResultsAdapter, MGRecyclerDataPayload>(Utils.getResId("widget_manage_reactions_result_error", "layout"), adapter) {

        val imageView: ImageView = itemView.findViewById(Utils.getResId("manage_reactions_result_error_img", "id"))

        override fun onConfigure(position: Int, payload: MGRecyclerDataPayload) {
            val data = payload as ErrorItem
            imageView.setOnClickListener {
                PollsStore.fetchDetails(data.channelId, data.messageId, data.answerId)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MGRecyclerViewHolder<ManageReactionsResultsAdapter, MGRecyclerDataPayload> =
        when (viewType) {
            0 -> ReactionUserViewHolder(this)
            1 -> LoadingViewHolder(this)
            3 -> EmptyViewHolder(this)
            4 -> ErrorViewHolder(this)
            else -> throw invalidViewTypeException(viewType)
        }
}

