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

internal class PollCreateViewModel : ViewModel() {
    val logger = Logger("Polls")

    var onModelUpdate: ((newModel: Model, previousModel: Model) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(model, model)
        }

    var model = Model()
        private set(value) {
            if (field == value)
                return
            val previousModel = field
            field = value
            onModelUpdate?.invoke(value, previousModel)
        }

    fun updateQuestionText(text: Editable) {
        model = model.copy(question = text.toString())
    }

    fun createAnswer() {
        if (model.answers.size >= 10) {
            logger.warn("newAnswer() called, but there's already max answers? Ignoring..")
            return
        }
        model = model.copy(answers = model.answers + listOf(AnswerModel()))
    }

    fun deleteAnswer(index: Int) {
        model = model.copy(answers = model.answers.filterIndexed { idx, _ -> idx != index })
    }

    fun updateAnswerText(index: Int, text: Editable) {
        model = model.copy(answers = model.answers.mapIndexed { idx, answer ->
            if (idx == index) {
                answer.copy(answer = text.toString())
            } else {
                answer
            }
        })
    }

    fun showEmojiSelector(index: Int, fragmentManager: FragmentManager) {
        val guild = StoreStream.getGuildSelected()
        val emojiCtx = if (guild != null) {
            EmojiPickerContextType.Guild(guild.selectedGuildId)
        } else {
            EmojiPickerContextType.Global.INSTANCE
        }
        WidgetEmojiPickerSheet.Companion!!.show(fragmentManager, {
            model = model.copy(answers = model.answers.mapIndexed { idx, answer ->
                if (idx == index)
                    answer.copy(emoji = it)
                else
                    answer
            })
        }, emojiCtx, {})
    }

    fun updateIsMultiselect(isMultiselect: Boolean) {
        model = model.copy(isMultiselect = isMultiselect)
    }

    fun showDurationSelector(fragmentManager: FragmentManager) {
        DurationSelectorSheet(model.duration) {
            model = model.copy(duration = it)
        }.show(fragmentManager, DurationSelectorSheet::class.java.name)
    }

    private fun buildPayload(): PollCreatePayload =
        PollCreatePayload(MessagePoll(
            question = MessagePollMedia(model.question, null),
            answers = model.answers.map {
                MessagePollAnswer(null, MessagePollMedia(it.answer, it.emoji?.asReactionEmoji()))
            },
            results = null,
            duration = model.duration.value,
            expiry = null,
            allowMultiselect = model.isMultiselect,
            layoutType = 1
        ))

    fun sendRequest(channelId: Long) {
        model = model.copy(requestState = RequestState.REQUESTING)
        Utils.threadPool.execute {
            val request = runCatching {
                Http.Request.newDiscordRNRequest(
                    "/channels/${channelId}/messages",
                    "POST"
                ).setRequestTimeout(10000).executeWithJson(buildPayload())
            }
            val result = request.getOrNull()
            val newState = if (result?.ok() != true) {
                logger.errorToast("Failed to create poll")
                if (result != null) {
                    logger.error("${result.statusCode} ${result.statusMessage} ${result.text()}", null)
                } else {
                    logger.error(request.exceptionOrNull())
                }
                RequestState.IDLE
            } else {
                RequestState.SUCCESS
            }
            Utils.mainThread.post { model = model.copy(requestState = newState) }
        }
    }
}
