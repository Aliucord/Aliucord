@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls

import com.aliucord.*
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.wrappers.messages.MessageWrapper.Companion.poll
import com.discord.api.message.poll.*
import com.discord.models.deserialization.gson.InboundGatewayGsonParser
import com.discord.models.user.CoreUser
import com.discord.models.user.User
import com.discord.stores.StoreStream
import com.google.gson.stream.JsonReader
import rx.Subscription
import rx.subjects.PublishSubject
import com.discord.api.message.Message as ApiMessage
import com.discord.api.user.User as ApiUser
import com.discord.models.message.Message as ModelMessage

object PollsStore {
    private val logger = Logger("Store/Polls")
    private data class ResponsePayload(val users: List<ApiUser>)

    data class VoteEvent(
        val channelId: Long,
        val messageId: Long,
        val voteSnapshots: HashMap<Int, VoterSnapshot>?
    )

    sealed class VoterSnapshot {
        data class Loading(val lastCount: Int, val lastSelfVote: Boolean) : VoterSnapshot() {
            companion object {
                fun from(snapshot: VoterSnapshot?) = Loading(snapshot?.count ?: 0, snapshot?.meVoted == true)
            }
        }
        data class Failed(val lastCount: Int, val lastSelfVote: Boolean) : VoterSnapshot() {
            companion object {
                fun from(snapshot: VoterSnapshot?) = Failed(snapshot?.count ?: 0, snapshot?.meVoted == true)
            }
        }
        data class Lazy(val voterCount: Int, val selfVoted: Boolean) : VoterSnapshot() {
            companion object {
                fun from(snapshot: VoterSnapshot?) = Lazy(snapshot?.count ?: 0, snapshot?.meVoted == true)
            }
        }
        data class Detailed(val voters: List<User>) : VoterSnapshot()

        val count: Int
            get() = when (this) {
                is Loading -> lastCount
                is Failed -> lastCount
                is Lazy -> voterCount
                is Detailed -> voters.size
            }

        val meVoted: Boolean
            get() = when (this) {
                is Loading -> lastSelfVote
                is Failed -> lastSelfVote
                is Lazy -> selfVoted
                is Detailed -> {
                    val me = StoreStream.getUsers().me
                    voters.any { it.id == me.id }
                }
            }

        companion object {
            val Empty
                get() = Detailed(listOf())
        }
    }

    private val snapshots = HashMap<Long, HashMap<Int, VoterSnapshot>>()
    private val subject = PublishSubject.k0<VoteEvent?>()

    fun dispatchGatewayEvent(event: MessagePollVoteEvent, isAdd: Boolean) {
        StoreStream.getDispatcherYesThisIsIntentional().schedule {
            handleGatewayEvent(event, isAdd)
        }
    }

    private fun handleGatewayEvent(event: MessagePollVoteEvent, isAdd: Boolean) {
        val isSelfVote = event.userId == StoreStream.getUsers().me.id

        val answers = snapshots[event.messageId]
        if (answers == null)
            return

        val snapshot = answers[event.answerId]
        answers[event.answerId] = when (snapshot) {
            is VoterSnapshot.Lazy -> {
                val voterCount = snapshot.voterCount + (if (isAdd) +1 else -1)

                if (isSelfVote)
                    VoterSnapshot.Lazy(voterCount, isAdd)
                else
                    snapshot.copy(voterCount = voterCount)
            }
            is VoterSnapshot.Detailed -> {
                val user = StoreStream.getUsers().users[event.userId]
                // TODO: Should we fetch the user, or just downgrade?
                if (user == null) {
                    logger.warn("New voter is not in store, downgrading")
                    VoterSnapshot.Lazy(snapshot.voters.size + 1, isSelfVote)
                } else
                    VoterSnapshot.Detailed(snapshot.voters.toMutableList().apply {
                        if (isAdd) add(user) else remove(user)
                    })
            }
            else -> {
                val user = StoreStream.getUsers().users[event.userId]
                if (user == null) {
                    logger.warn("New voter is not in store, downgrading")
                    VoterSnapshot.Lazy(1, isSelfVote)
                } else
                    VoterSnapshot.Detailed(listOf(user))
            }
        }
        publish(event.channelId, event.messageId)
    }

    fun handleMessageUpdate(message: ApiMessage) =
        handleMessageUpdate(message.g(), message.o(), message.poll)
    fun handleMessageUpdate(message: ModelMessage) =
        handleMessageUpdate(message.channelId, message.id, message.poll)

    private fun handleMessageUpdate(channelId: Long, messageId: Long, poll: MessagePoll?) {
        if (poll == null)
            return

        val target = HashMap<Int, VoterSnapshot>()
        poll.results?.answerCounts?.forEach {
            target[it.id] = VoterSnapshot.Lazy(it.count, it.meVoted)
        }
        snapshots[messageId] = target
        publish(channelId, messageId)
    }

    fun handleMessageDelete(channelId: Long, messageId: Long) {
        snapshots.remove(messageId)
        publish(channelId, messageId)
    }

    private fun publish(channelId: Long, messageId: Long) {
        subject.onNext(VoteEvent(channelId, messageId, snapshots[messageId]))
    }

    fun subscribeOnMain(channelId: Long, messageId: Long, onNext: (HashMap<Int, VoterSnapshot>?) -> Unit) =
        subscribe(channelId, messageId) {
            Utils.mainThread.post { onNext(it) }
        }

    fun subscribe(channelId: Long, messageId: Long, onNext: (HashMap<Int, VoterSnapshot>?) -> Unit): Subscription {
        snapshots[messageId]?.let { onNext(it) }
        return subject.subscribe {
            if (this.channelId == channelId && this.messageId == messageId)
                onNext(snapshots[messageId])
        }
    }

    fun fetchDetails(channelId: Long, messageId: Long, answerId: Int) {
        val answers = snapshots[messageId]
        if (answers == null) {
            Logger("PollsStore").warn("Attempted to fetch details for uninitialised poll")
            return
        }

        val lastSnapshot = answers[answerId]
        if (lastSnapshot is VoterSnapshot.Loading || lastSnapshot is VoterSnapshot.Detailed)
            return

        answers[answerId] = VoterSnapshot.Loading.from(lastSnapshot)
        publish(channelId, messageId)
        Utils.threadPool.execute {
            val request = runCatching {
                val res = Http.Request.newDiscordRNRequest("/channels/${channelId}/polls/${messageId}/answers/${answerId}").execute()
                res to InboundGatewayGsonParser.fromJson(JsonReader(res.stream().reader()), ResponsePayload::class.java).users
            }
            val (result, data) = request.getOrNull() ?: (null to listOf())
            answers[answerId] = if (result?.ok() != true) {
                logger.errorToast("Failed to fetch poll results")
                if (result != null)
                    logger.error("${result.statusCode} ${result.statusMessage} ${result.text()}", null)
                else
                    logger.error(request.exceptionOrNull())
                VoterSnapshot.Failed.from(lastSnapshot)
            } else {
                VoterSnapshot.Detailed(data.map { CoreUser(it) })
            }
            StoreStream.getDispatcherYesThisIsIntentional().schedule {
                publish(channelId, messageId)
            }
        }
    }

    fun getResultFor(id: Long, finalised: Boolean) = snapshots[id]?.let {
        MessagePollResult(finalised, it.map { (answerId, snapshot) ->
            MessagePollAnswerCount(answerId, snapshot.count, snapshot.meVoted)
        })
    }
}
