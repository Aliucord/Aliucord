@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils
import com.aliucord.wrappers.messages.MessageWrapper.Companion.poll
import com.discord.api.message.poll.MessagePoll
import com.discord.api.message.poll.MessagePollAnswer
import com.discord.api.message.reaction.MessageReactionEmoji
import com.discord.app.AppFragment
import com.discord.stores.StoreStream
import com.discord.utilities.drawable.DrawableCompat
import com.discord.utilities.mg_recycler.*
import com.discord.utilities.textprocessing.node.EmojiNode
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.widgets.chat.managereactions.ManageReactionsResultsAdapter
import com.discord.widgets.chat.managereactions.ManageReactionsResultsAdapter.*
import com.lytefast.flexinput.R
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

    val containerView: LinearLayout = itemView.findViewById(Utils.getResId("manage_reactions_emoji_container", "id"))
    val emojiView: SimpleDraweeSpanTextView = itemView.findViewById(Utils.getResId("manage_reactions_emoji_emoji_textview", "id"))
    val textView: TextView = itemView.findViewById(Utils.getResId("manage_reactions_emoji_counter", "id"))
    val selectedIndicatorView: View = itemView.findViewById(Utils.getResId("manage_reactions_emoji_selected_indicator", "id"))

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

@SuppressLint("SetTextI18n")
internal class EmptyViewHolder(adapter: ManageReactionsResultsAdapter)
    : MGRecyclerViewHolder<ManageReactionsResultsAdapter, MGRecyclerDataPayload>(Utils.getResId("widget_manage_reactions_result_error", "layout"), adapter) {

    val imageView: ImageView = itemView.findViewById(Utils.getResId("manage_reactions_result_error_img", "id"))
    val textView: TextView = itemView.findViewById(Utils.getResId("manage_reactions_result_error_text", "id"))

    init {
        imageView.setImageResource(DrawableCompat.getThemedDrawableRes(itemView, R.b.theme_friends_no_friends))
        textView.text = "There are no votes for this answer"
    }
}

internal class EmptyItem : MGRecyclerDataPayload {
    override fun getKey() = "3"
    override fun getType() = 3
}

@SuppressLint("SetTextI18n")
internal class PollErrorViewHolder(adapter: ManageReactionsResultsAdapter)
    : MGRecyclerViewHolder<ManageReactionsResultsAdapter, MGRecyclerDataPayload>(Utils.getResId("widget_manage_reactions_result_error", "layout"), adapter) {

    val imageView: ImageView = itemView.findViewById(Utils.getResId("manage_reactions_result_error_img", "id"))

    override fun onConfigure(position: Int, payload: MGRecyclerDataPayload) {
        val data = payload as PollErrorItem
        imageView.setOnClickListener {
            PollsStore.fetchDetails(data.channelId, data.messageId, data.answerId)
        }
    }
}

internal class PollErrorItem(
    val channelId: Long,
    val messageId: Long,
    val answerId: Int,
) : MGRecyclerDataPayload {
    override fun getKey() = "4"
    override fun getType() = 4
}

internal class PollVotesDetailResultsAdapter(recyclerView: RecyclerView) : ManageReactionsResultsAdapter(recyclerView) {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MGRecyclerViewHolder<ManageReactionsResultsAdapter, MGRecyclerDataPayload> =
        when (viewType) {
            0 -> ReactionUserViewHolder(this)
            1 -> LoadingViewHolder(this)
            3 -> EmptyViewHolder(this)
            4 -> PollErrorViewHolder(this)
            else -> throw invalidViewTypeException(viewType)
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
    private var subscription: Subscription? = null

    lateinit var poll: MessagePoll
    var selected: Int = 1
        private set(value) {
            field = value
            update(true)
        }

    var data: Map<Int, PollsStore.VoterSnapshot>? = null

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        val answersView = view.findViewById<RecyclerView>(Utils.getResId("manage_reactions_emojis_recycler", "id"))
        val resultsView = view.findViewById<RecyclerView>(Utils.getResId("manage_reactions_results_recycler", "id"))
        answersAdapter = configure(PollVotesDetailAnswersAdapter(answersView) { selected = it })
        resultsAdapter = configure(PollVotesDetailResultsAdapter(resultsView))

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
            data = it
            update()
        }
    }

    private fun update(attemptRetry: Boolean = false) {
        val data = data
        if (data == null)
            return

        answersAdapter.setData(poll.answers.map { answer ->
            val count = data[answer.answerId]?.count ?: 0
            PollAnswerItem(answer, count, answer.answerId == selected)
        })

        val selectedDetails = data[selected]
        val map = StoreStream.getGuilds().members[StoreStream.getGuildSelected().selectedGuildId]
        val payload = if (selectedDetails?.count == 0 && selectedDetails !is PollsStore.VoterSnapshot.Failed)
            listOf(EmptyItem())
        else when (selectedDetails) {
            null, is PollsStore.VoterSnapshot.Lazy -> {
                PollsStore.fetchDetails(channelId, messageId, selected)
                listOf(LoadingItem())
            }
            is PollsStore.VoterSnapshot.Loading -> listOf(LoadingItem())
            is PollsStore.VoterSnapshot.Failed -> {
                if (attemptRetry) {
                    PollsStore.fetchDetails(channelId, messageId, selected)
                    listOf(LoadingItem())
                } else
                    listOf(PollErrorItem(channelId, messageId, selected))
            }
            is PollsStore.VoterSnapshot.Detailed -> selectedDetails.voters.map { ReactionUserItem(it, 0, 0, MessageReactionEmoji("", "", false), false, map?.get(it.id)) }
        }
        resultsAdapter.setData(payload)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription?.unsubscribe()
    }
}
