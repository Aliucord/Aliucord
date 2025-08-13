package com.aliucord.coreplugins.polls.chatview

import com.aliucord.*
import com.aliucord.coreplugins.polls.PollsStore
import com.aliucord.utils.SerializedName
import com.discord.api.message.poll.MessagePoll
import com.discord.models.message.Message
import rx.Subscription

internal class PollChatViewModel(
    msg: Message,
    poll: MessagePoll,
    var onModelUpdate: (PollChatView.Model, isUpdate: Boolean) -> Unit,
) {
    private data class PollVotePayload(
        @SerializedName("answer_ids")
        val answerIds: List<Int>
    )

    private val logger = Logger("Polls")
    var model: PollChatView.Model
        private set
    private var subscription: Subscription? = null

    init {
        val results = PollsStore.getResultFor(msg.id, poll.results?.isFinalized ?: false)
            ?: poll.results
        model = PollChatView.Model(
            msg.channelId,
            msg.id,
            poll.question.text,
            poll.allowMultiselect,
            poll.expiry!!.g(),
            finalised = results?.isFinalized ?: false,
            peekingResults = false,
            submittingVote = false,
            poll.answers.map {
                val count = results?.answerCounts?.find { count -> count.id == it.answerId }
                PollChatView.AnswerModel(
                    it.answerId!!,
                    it.pollMedia.text,
                    it.pollMedia.emoji,
                    meVoted = count?.meVoted ?: false,
                    votes = count?.count ?: 0,
                    checked = false
                )
            }
        )
        subscribe()
    }

    private fun setModel(model: PollChatView.Model, isUpdate: Boolean = false) {
        this.model = model
        onModelUpdate(model, isUpdate)
    }

    fun subscribe() {
        unsubscribe()
        subscription = PollsStore.subscribeOnMain(model.channelId, model.messageId) { snapshots ->
            if (snapshots == null) {
                unsubscribe()
                return@subscribeOnMain
            }
            setModel(
                model.copy(
                answers = model.answers.map {
                    val snapshot = snapshots[it.id]!!
                    it.copy(
                        meVoted = snapshot.meVoted,
                        votes = snapshot.count
                    )
                }
            ), true)
        }
    }

    fun unsubscribe() = subscription?.unsubscribe()
    fun addVotes() = submitVote(true)
    fun removeVotes() = submitVote(false)
    fun peekResults() = setModel(model.copy(peekingResults = true))
    fun unpeekResults() = setModel(model.copy(peekingResults = false))

    fun toggleVote(id: Int) {
        setModel(model.copy(
            answers = model.answers.map {
                val checked = when {
                    it.id == id -> !it.checked
                    !model.multiselect -> false
                    else -> it.checked
                }
                it.copy(checked = checked)
            }
        ))
    }

    private fun submitVote(isAdd: Boolean) {
        setModel(model.copy(submittingVote = true))
        Utils.threadPool.execute {
            val request = runCatching {
                Http.Request.newDiscordRNRequest(
                    "/channels/${model.channelId}/polls/${model.messageId}/answers/@me",
                    "PUT"
                ).setRequestTimeout(10000).executeWithJson(
                    PollVotePayload(
                        answerIds = if (isAdd) {
                            model.answers.filter { it.checked }.map { it.id }
                        } else {
                            listOf()
                        }
                    )
                )
            }
            val result = request.getOrNull()
            if (result?.ok() != true) {
                logger.errorToast("Failed to submit poll vote")
                if (result != null) {
                    logger.error("${result.statusCode} ${result.statusMessage} ${result.text()}", null)
                } else {
                    logger.error(request.exceptionOrNull())
                }
            }
            Utils.mainThread.post { setModel(model.copy(
                submittingVote = false,
                answers = model.answers.map { it.copy(checked = false) }
            )) }
        }
    }
}
