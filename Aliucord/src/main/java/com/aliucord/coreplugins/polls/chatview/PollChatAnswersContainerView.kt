package com.aliucord.coreplugins.polls.chatview

import android.content.Context
import com.aliucord.coreplugins.polls.PollsStore.VotesSnapshot
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.Divider
import com.aliucord.widgets.LinearLayout
import com.aliucord.wrappers.messages.poll
import com.discord.models.message.Message

internal class PollChatAnswersContainerView(private val ctx: Context) : LinearLayout(ctx) {
    private val answerViews = HashMap<Int, PollChatAnswerView>()

    val hasChecked get() = getCheckedAnswers().isNotEmpty()
    var onHasCheckedChange: ((Boolean) -> Unit)? = null

    fun configure(data: Message) {
        val poll = data.poll!!
        removeAllViews()
        answerViews.clear()

        Divider(ctx).addTo(this)
        for (answer in poll.answers) {
            PollChatAnswerView.build(ctx, data.channelId, data.id, answer, poll.allowMultiselect) {
                for (answerView in answerViews.values)
                    if (answerView !== this && !poll.allowMultiselect)
                        answerView.isChecked = false
                    else if (answerView === this)
                        answerView.isChecked = !answerView.isChecked
                onHasCheckedChange?.invoke(getCheckedAnswers().isNotEmpty())
            }.addTo(this) {
                answerViews[answer.answerId!!] = this
            }
            Divider(ctx).addTo(this)
        }
    }

    fun updateCounts(counts: Map<Int, VotesSnapshot>, state: PollChatView.State) {
        val total = counts.values.sumOf { it.count }.coerceAtLeast(1) // Prevent division by 0
        var winner = counts.values.maxOfOrNull { it.count } ?: -1
        if (winner == 0)
            winner = -1 // There is no winner if there are no votes
        for ((id, answerView) in answerViews) {
            val count = counts.getOrElse(id) { VotesSnapshot.Detailed() }
            answerView.updateCount(
                count,
                total,
                count.count == winner && state == PollChatView.State.FINALISED,
                state
            )
        }
    }

    fun updateState(state: PollChatView.State, shouldReanimate: Boolean) {
        for (answer in answerViews.values)
            answer.updateState(state, shouldReanimate)
    }

    fun getCheckedAnswers(): Set<Int> =
        answerViews.filter { (_, answerView) -> answerView.isChecked }.keys
}
