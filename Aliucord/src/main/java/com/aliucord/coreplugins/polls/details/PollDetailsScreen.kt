@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls.details

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.coreplugins.polls.PollsStore
import com.aliucord.coreplugins.polls.PollsStore.VoterSnapshot
import com.aliucord.wrappers.messages.MessageWrapper.Companion.poll
import com.discord.api.message.poll.MessagePoll
import com.discord.api.message.reaction.MessageReactionEmoji
import com.discord.app.AppFragment
import com.discord.stores.StoreStream
import com.discord.utilities.mg_recycler.MGRecyclerAdapter
import com.discord.widgets.chat.managereactions.ManageReactionsResultsAdapter
import rx.Subscription

internal class PollDetailsScreen : AppFragment(Utils.getResId("widget_manage_reactions", "layout")) {
    companion object {
        fun create(ctx: Context, channelId: Long, messageId: Long) {
            val intent = Intent()
                .putExtra("com.discord.intent.extra.EXTRA_CHANNEL_ID", channelId)
                .putExtra("com.discord.intent.extra.EXTRA_MESSAGE_ID", messageId)
            Utils.openPage(ctx, PollDetailsScreen::class.java, intent)
        }
    }

    var channelId: Long = -1
        private set

    var messageId: Long = -1
        private set

    private lateinit var answersAdapter: PollDetailsAnswersAdapter
    private lateinit var resultsAdapter: PollDetailsResultsAdapter
    private var subscription: Subscription? = null

    lateinit var poll: MessagePoll
    var selected: Int = 1
        private set(value) {
            field = value
            update(true)
        }

    var data: Map<Int, VoterSnapshot>? = null

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        val answersView = view.findViewById<RecyclerView>(Utils.getResId("manage_reactions_emojis_recycler", "id"))
        val resultsView = view.findViewById<RecyclerView>(Utils.getResId("manage_reactions_results_recycler", "id"))
        answersAdapter = MGRecyclerAdapter.configure(PollDetailsAnswersAdapter(answersView) {
            selected = it
        })
        resultsAdapter = MGRecyclerAdapter.configure(PollDetailsResultsAdapter(resultsView))

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

        subscription = PollsStore.subscribeOnMain(channelId, messageId) {
            data = it
            update()
        }
    }

    private fun update(attemptRetry: Boolean = false) {
        val data = data
        if (data == null)
            return this.appActivity.finish()

        answersAdapter.setData(poll.answers.map { answer ->
            val count = data[answer.answerId]?.count ?: 0
            PollDetailsAnswersAdapter.AnswerItem(answer, count, answer.answerId == selected)
        })

        val selectedDetails = data[selected]
        val map = StoreStream.getGuilds().members[StoreStream.getGuildSelected().selectedGuildId]
        val payload = if (selectedDetails?.count == 0 && selectedDetails !is VoterSnapshot.Failed)
            listOf(PollDetailsResultsAdapter.EmptyItem())
        else when (selectedDetails) {
            null, is VoterSnapshot.Lazy -> {
                PollsStore.fetchDetails(channelId, messageId, selected)
                listOf(ManageReactionsResultsAdapter.LoadingItem())
            }
            is VoterSnapshot.Loading -> listOf(ManageReactionsResultsAdapter.LoadingItem())
            is VoterSnapshot.Failed -> {
                if (attemptRetry) {
                    PollsStore.fetchDetails(channelId, messageId, selected)
                    listOf(ManageReactionsResultsAdapter.LoadingItem())
                } else
                    listOf(PollDetailsResultsAdapter.ErrorItem(channelId, messageId, selected))
            }
            is VoterSnapshot.Detailed -> selectedDetails.voters.map {
                ManageReactionsResultsAdapter.ReactionUserItem(
                    it,
                    0,
                    0,
                    MessageReactionEmoji("", "", false),
                    false,
                    map?.get(it.id)
                )
            }
        }
        resultsAdapter.setData(payload)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription?.unsubscribe()
    }
}
