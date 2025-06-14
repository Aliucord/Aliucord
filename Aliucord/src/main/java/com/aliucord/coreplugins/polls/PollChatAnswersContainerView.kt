package com.aliucord.coreplugins.polls

import android.content.Context
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.Divider
import com.aliucord.widgets.LinearLayout
import com.discord.api.message.poll.*

internal class PollChatAnswersContainerView(private val ctx: Context) : LinearLayout(ctx) {
    private val answerViews = HashMap<Int, PollChatAnswerView>()

    val hasClicked get() = getCheckedAnswers().count() > 0
    var onHasClickedChange: ((Boolean) -> Unit)? = null

    fun configure(data: MessagePoll) {
        removeAllViews()
        answerViews.clear()
        updateChecked()

        Divider(ctx).addTo(this)
        for (answer in data.answers) {
            PollChatAnswerView.build(ctx, answer, data.allowMultiselect).addTo(this) {
                answerViews[answer.answerId!!] = this
                e {
                    for (answerView in answerViews.values)
                        if (answerView !== this && !data.allowMultiselect)
                            answerView.isChecked = false
                        else if (answerView === this)
                            answerView.isChecked = !answerView.isChecked
                    updateChecked()
                }
            }
            Divider(ctx).addTo(this)
        }
    }

    fun updateCounts(results: MessagePollResult, state: PollChatView.State) {
        val counts = results.answerCounts
        val total = counts.sumOf { it.count }.coerceAtLeast(1) // Prevent division by 0
        var winner = counts.maxOfOrNull { it.count } ?: -1
        if (winner == 0)
            winner = -1 // There is no winner if there are no votes
        for ((id, answerView) in answerViews) {
            val count = counts.find { it.id == id } ?: MessagePollAnswerCount(0, 0, false)
            answerView.updateCount(
                count,
                total,
                count.count == winner && state == PollChatView.State.FINALISED,
                state
            )
        }
    }

    fun updateState(state: PollChatView.State, isTransition: Boolean) {
        for (answer in answerViews.values)
            answer.updateState(state, isTransition)
    }

    private fun updateChecked() {
        onHasClickedChange?.invoke(getCheckedAnswers().count() > 0)
    }

    fun getCheckedAnswers(): Iterable<Int> =
        answerViews.filter { (_, answerView) -> answerView.isChecked }.keys
}
