@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls.details

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.coreplugins.polls.PollsStore
import com.aliucord.coreplugins.polls.PollsStore.VotesSnapshot
import com.aliucord.wrappers.messages.poll
import com.discord.api.message.poll.MessagePoll
import com.discord.api.message.reaction.MessageReactionEmoji
import com.discord.app.AppFragment
import com.discord.stores.StoreStream
import com.discord.utilities.mg_recycler.MGRecyclerAdapter
import com.discord.widgets.chat.managereactions.ManageReactionsResultsAdapter
import rx.Subscription

internal class PollDetailsScreen : AppFragment(Utils.getResId("widget_manage_reactions", "layout")) {
    companion object {
        fun launch(ctx: Context, channelId: Long, messageId: Long, initialAnswerId: Int = 1) {
            val intent = Intent()
                .putExtra("com.discord.intent.extra.EXTRA_CHANNEL_ID", channelId)
                .putExtra("com.discord.intent.extra.EXTRA_MESSAGE_ID", messageId)
                .putExtra("com.discord.intent.extra.EXTRA_ANSWER_ID", initialAnswerId)
            Utils.openPage(ctx, PollDetailsScreen::class.java, intent)
        }
    }

    private var channelId: Long = -1
    private var messageId: Long = -1

    private lateinit var answersAdapter: PollDetailsAnswersAdapter
    private lateinit var resultsAdapter: PollDetailsResultsAdapter
    private var subscription: Subscription? = null

    lateinit var poll: MessagePoll
    private var selected: Int = -1
        set(value) {
            val previous = field
            field = value
            if (previous != -1) {
                updateData(true)
            }
        }

    var data: Map<Int, VotesSnapshot>? = null

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        channelId = mostRecentIntent.getLongExtra("com.discord.intent.extra.EXTRA_CHANNEL_ID", -1L)
        messageId = mostRecentIntent.getLongExtra("com.discord.intent.extra.EXTRA_MESSAGE_ID", -1L)
        selected = mostRecentIntent.getIntExtra("com.discord.intent.extra.EXTRA_ANSWER_ID", 1)

        if (channelId == -1L || messageId == -1L) {
            return this.appActivity.finish()
        }

        poll = StoreStream.getMessages().getMessage(channelId, messageId)?.poll
            ?: return this.appActivity.finish()

        val answersView = view.findViewById<RecyclerView>(Utils.getResId("manage_reactions_emojis_recycler", "id"))
        val resultsView = view.findViewById<RecyclerView>(Utils.getResId("manage_reactions_results_recycler", "id"))
        answersAdapter = MGRecyclerAdapter.configure(PollDetailsAnswersAdapter(answersView) {
            selected = it
        })
        resultsAdapter = MGRecyclerAdapter.configure(PollDetailsResultsAdapter(resultsView) {
            PollsStore.fetchDetails(channelId, messageId, selected)
        })

        setActionBarDisplayHomeAsUpEnabled()
        setActionBarTitle("Poll Votes")
        setActionBarSubtitle(poll.question.text)

        subscription = PollsStore.subscribeOnMain(channelId, messageId) {
            data = it
            updateData()
        }
    }

    private fun updateData(attemptRetry: Boolean = false) {
        val snapshots = data
        if (snapshots == null) { // Message is deleted
            this.appActivity.finish()
            return
        }

        answersAdapter.setData(poll.answers.map { answer ->
            val count = snapshots[answer.answerId]?.count ?: 0
            PollDetailsAnswersAdapter.AnswerItem(answer, count, answer.answerId == selected)
        })

        val snapshot = snapshots[selected]!!
        val usersMap = StoreStream.getUsers().users
        val membersMap = StoreStream.getGuilds().members[StoreStream.getGuildSelected().selectedGuildId]
        val payload = when {
            snapshot.count == 0 -> {
                listOf(PollDetailsResultsAdapter.EmptyItem())
            }
            snapshot.hasFailed && !attemptRetry -> {
                listOf(PollDetailsResultsAdapter.ErrorItem(channelId, messageId, selected))
            }
            snapshot.voters.isEmpty() -> {
                PollsStore.fetchDetails(channelId, messageId, selected)
                listOf(ManageReactionsResultsAdapter.LoadingItem())
            }
            else -> {
                val userItems = snapshot.voters.map {
                    ManageReactionsResultsAdapter.ReactionUserItem(
                        usersMap[it],
                        0,
                        0,
                        MessageReactionEmoji("", "", false),
                        false,
                        membersMap?.get(it)
                    )
                }
                if (snapshot.isIncomplete) {
                    userItems + PollDetailsResultsAdapter.MiniLoadingItem()
                }
                else {
                    userItems
                }
            }
        }
        resultsAdapter.setData(payload)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription?.unsubscribe()
    }
}
