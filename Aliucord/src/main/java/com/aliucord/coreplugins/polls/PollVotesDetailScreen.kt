@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils
import com.aliucord.wrappers.messages.MessageWrapper.Companion.poll
import com.discord.api.message.poll.MessagePoll
import com.discord.api.message.poll.MessagePollAnswer
import com.discord.api.message.reaction.MessageReactionEmoji
import com.discord.app.AppFragment
import com.discord.stores.StoreStream
import com.discord.utilities.mg_recycler.*
import com.discord.utilities.textprocessing.node.EmojiNode
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.widgets.chat.managereactions.ManageReactionsResultsAdapter
import com.discord.widgets.chat.managereactions.ManageReactionsResultsAdapter.*
import rx.Subscription

internal data class PollAnswerItem(
    val answer: MessagePollAnswer,
    val voteCount: Int,
    val isSelected: Boolean,
) : MGRecyclerDataPayload {
    override fun getKey() = answer.answerId.toString()
    override fun getType() = 0
}

internal class PollAnswerItemViewHolder(adapter: PollVotesDetailAnswersAdapter)
    : MGRecyclerViewHolder<PollVotesDetailAnswersAdapter, PollAnswerItem>(Utils.getResId("widget_manage_reactions_emoji", "layout"), adapter) {

    val containerView = itemView.findViewById<LinearLayout>(Utils.getResId("manage_reactions_emoji_container", "id"))
    val emojiView = itemView.findViewById<SimpleDraweeSpanTextView>(Utils.getResId("manage_reactions_emoji_emoji_textview", "id"))
    val textView = itemView.findViewById<TextView>(Utils.getResId("manage_reactions_emoji_counter", "id"))
    val selectedIndicatorView = itemView.findViewById<View>(Utils.getResId("manage_reactions_emoji_selected_indicator", "id"))

    @SuppressLint("SetTextI18n")
    override fun onConfigure(position: Int, data: PollAnswerItem) {
        super.onConfigure(position, data)

        EmojiNode.Companion!!.renderEmoji(emojiView, data.answer.pollMedia.emoji, true, DimenUtils.dpToPx(20))
        textView.text = "${data.answer.pollMedia.text} (${data.voteCount})"
        selectedIndicatorView.visibility = if (data.isSelected) View.VISIBLE else View.INVISIBLE
        containerView.setOnClickListener {
            adapter.onAnswerSelected(data.answer.answerId!!)
        }
    }
}

internal class PollVotesDetailAnswersAdapter(
    recyclerView: RecyclerView,
    val onAnswerSelected: (answerId: Int) -> Unit
) : MGRecyclerAdapterSimple<PollAnswerItem>(recyclerView) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MGRecyclerViewHolder<PollVotesDetailAnswersAdapter, PollAnswerItem> {
        if (viewType != 0)
            throw invalidViewTypeException(viewType)
        return PollAnswerItemViewHolder(this)
    }
}

internal class PollVotesDetailScreen : AppFragment(Utils.getResId("widget_manage_reactions", "layout")) {
    companion object {
        fun create(ctx: Context, channelId: Long, messageId: Long) {
            val intent = Intent()
                .putExtra("com.discord.intent.extra.EXTRA_CHANNEL_ID", channelId)
                .putExtra("com.discord.intent.extra.EXTRA_MESSAGE_ID", messageId)
            Utils.openPage(ctx, PollVotesDetailScreen::class.java, intent)
        }
    }

    var channelId: Long = -1
        private set

    var messageId: Long = -1
        private set

    private lateinit var answersAdapter: PollVotesDetailAnswersAdapter
    private lateinit var resultsAdapter: ManageReactionsResultsAdapter
    private lateinit var subscription: Subscription

    lateinit var poll: MessagePoll
    var selected: Int = 1
        private set(value) {
            field = value
            PollsStore.fetchDetails(channelId, messageId, value)
        }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        val answersView = view.findViewById<RecyclerView>(Utils.getResId("manage_reactions_emojis_recycler", "id"))
        val resultsView = view.findViewById<RecyclerView>(Utils.getResId("manage_reactions_results_recycler", "id"))
        answersAdapter = configure(PollVotesDetailAnswersAdapter(answersView) { selected = it })
        resultsAdapter = configure(ManageReactionsResultsAdapter(resultsView))

        channelId = mostRecentIntent.getLongExtra("com.discord.intent.extra.EXTRA_CHANNEL_ID", -1L)
        messageId = mostRecentIntent.getLongExtra("com.discord.intent.extra.EXTRA_MESSAGE_ID", -1L)
        if (channelId == -1L || messageId == -1L)
            return this.appActivity.finish()

        val poll = StoreStream.getMessages().getMessage(channelId, messageId)?.poll
        if (poll == null)
            return this.appActivity.finish()
        this.poll = poll

        setActionBarDisplayHomeAsUpEnabled()
        setActionBarTitle("Poll Votes");
        setActionBarSubtitle(poll.question.text)

        subscription = PollsStore.subscribe(channelId, messageId) {
            Logger("pvs").info("subscription! ${it.toString()}")
            answersAdapter.setData(poll.answers.map { answer ->
                val count = it[answer.answerId]?.count ?: 0
                PollAnswerItem(answer, count, answer.answerId == selected)
            })

            val selectedDetails = it[selected]
            val map = StoreStream.getGuilds().members[StoreStream.getGuildSelected().selectedGuildId]
            val payload = when (selectedDetails) {
                null, is PollsStore.VoterSnapshot.Lazy -> {
                    PollsStore.fetchDetails(channelId, messageId, selected)
                    listOf(LoadingItem())
                }
                is PollsStore.VoterSnapshot.Loading -> listOf(LoadingItem())
                is PollsStore.VoterSnapshot.Failed -> listOf(ErrorItem(0, 0, MessageReactionEmoji("", "", false)))
                is PollsStore.VoterSnapshot.Detailed -> selectedDetails.voters.map { ReactionUserItem(it, 0, 0, MessageReactionEmoji("", "", false), false, map?.get(it.id)) }
            }
            resultsAdapter.setData(payload)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription.unsubscribe()
    }
}
