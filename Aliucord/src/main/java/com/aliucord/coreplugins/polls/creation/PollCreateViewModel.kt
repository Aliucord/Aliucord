@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls.creation

import android.text.Editable
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.aliucord.*
import com.aliucord.coreplugins.polls.creation.PollCreateScreen.*
import com.aliucord.coreplugins.polls.creation.PollCreateScreen.Companion.asReactionEmoji
import com.discord.api.message.poll.*
import com.discord.stores.StoreStream
import com.discord.widgets.chat.input.emoji.EmojiPickerContextType
import com.discord.widgets.chat.input.emoji.WidgetEmojiPickerSheet

internal class PollCreateViewModel() : ViewModel() {
    val logger = Logger("Polls")

    var onStateUpdate: ((newState: State, previousState: State) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(state, state)
        }

    var state = State()
        private set(value) {
            if (field == value)
                return
            val previousState = field
            field = value
            onStateUpdate?.invoke(value, previousState)
        }

    fun updateQuestionText(text: Editable) {
        state = state.copy(question = text.toString())
    }

    fun createAnswer() {
        if (state.answers.size >= 10)
            return logger.warn("newAnswer() called, but there's already max answers? Ignoring..")
        state = state.copy(answers = state.answers + listOf(AnswerState()))
    }

    fun deleteAnswer(index: Int) {
        state = state.copy(answers = state.answers.filterIndexed { idx, _ -> idx != index })
    }

    fun updateAnswerText(index: Int, text: Editable) {
        state = state.copy(answers = state.answers.mapIndexed { idx, answer ->
            if (idx == index)
                answer.copy(answer = text.toString())
            else
                answer
        })
    }

    fun showEmojiSelector(index: Int, fragmentManager: FragmentManager) {
        val guild = StoreStream.getGuildSelected()
        val emojiCtx = if (guild != null)
            EmojiPickerContextType.Guild(guild.selectedGuildId)
        else
            EmojiPickerContextType.Global.INSTANCE
        WidgetEmojiPickerSheet.Companion!!.show(fragmentManager, {
            state = state.copy(answers = state.answers.mapIndexed { idx, answer ->
                if (idx == index)
                    answer.copy(emoji = it)
                else
                    answer
            })
        }, emojiCtx, {})
    }

    fun updateIsMultiselect(isMultiselect: Boolean) {
        state = state.copy(isMultiselect = isMultiselect)
    }

    fun showDurationSelector(fragmentManager: FragmentManager) {
        DurationSelectorSheet(state.duration) {
            state = state.copy(duration = it)
        }.show(fragmentManager, DurationSelectorSheet::class.java.name)
    }

    private fun buildPayload(): PollCreatePayload = PollCreatePayload(MessagePoll(
        question = MessagePollMedia(state.question.toString(), null),
        answers = state.answers.map {
            MessagePollAnswer(null, MessagePollMedia(it.answer.toString(), it.emoji?.asReactionEmoji()))
        },
        results = null,
        duration = state.duration.value,
        expiry = null,
        allowMultiselect = state.isMultiselect,
        layoutType = 1
    ))

    fun sendRequest(channelId: Long) {
        state = state.copy(requestState = RequestState.REQUESTING)
        Utils.threadPool.execute {
            val req = Http.Request.newDiscordRNRequest(
                "/channels/${channelId}/messages",
                "POST"
            ).setRequestTimeout(10000).executeWithJson(buildPayload())
            if (!req.ok()) {
                logger.errorToast("Failed to create poll")
                logger.error("${req.statusCode} ${req.statusMessage} ${req.text()}", null)
                state = state.copy(requestState = RequestState.IDLE)
            }
            state = state.copy(requestState = RequestState.SUCCESS)
        }
    }
}
