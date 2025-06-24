@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls

import com.aliucord.*
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.wrappers.messages.MessageWrapper.Companion.poll
import com.discord.models.deserialization.gson.InboundGatewayGsonParser
import com.discord.models.message.Message
import com.discord.models.user.CoreUser
import com.discord.models.user.User
import com.discord.stores.StoreStream
import com.discord.stores.StoreThread
import com.google.gson.stream.JsonReader
import rx.Subscription
import rx.subjects.PublishSubject
import com.discord.api.user.User as ApiUser

object PollsStore {
    private val logger = Logger("Store/Polls")
    private data class ResponsePayload(val users: List<ApiUser>)

    data class VoteEvent(
        val channelId: Long,
        val messageId: Long,
        val voteSnapshots: HashMap<Int, VoterSnapshot>
    )

    sealed class VoterSnapshot {
        data class Loading(val lastCount: Int) : VoterSnapshot()
        data class Failed(val lastCount: Int) : VoterSnapshot()
        data class Lazy(val voterCount: Int) : VoterSnapshot()
        data class Detailed(val voters: List<User>) : VoterSnapshot()

        val count: Int
            get() = when (this) {
                is Loading -> lastCount
                is Failed -> lastCount
                is Lazy -> voterCount
                is Detailed -> voters.size
            }
    }

    private val snapshots = HashMap<Long, HashMap<Int, VoterSnapshot>>()
    private val subject = PublishSubject.k0<VoteEvent>()

    @StoreThread
    fun handleGatewayEvent(event: MessagePollVoteEvent, isAdd: Boolean) {
        val answers = snapshots[event.messageId]
        if (answers == null) {
            Logger("PollsStore").warn("Attempted to publish an event without initialisation")
            return
        }
        val snapshot = answers[event.answerId]
        answers[event.answerId] = when (snapshot) {
            is VoterSnapshot.Lazy -> if (isAdd)
                VoterSnapshot.Lazy(snapshot.voterCount + 1)
            else
                VoterSnapshot.Lazy(snapshot.voterCount - 1)
            is VoterSnapshot.Detailed -> {
                val user = StoreStream.getUsers().users[event.userId]
                // TODO: Should we fetch the user, or just downgrade?
                if (user == null) {
                    logger.warn("New voter is not in store, downgrading")
                    VoterSnapshot.Lazy(snapshot.voters.size + 1)
                } else
                    VoterSnapshot.Detailed(snapshot.voters.toMutableList().apply {
                        if (isAdd) add(user) else remove(user)
                    })
            }
            else -> {
                val user = StoreStream.getUsers().users[event.userId]
                if (user == null) {
                    logger.warn("New voter is not in store, downgrading")
                    VoterSnapshot.Lazy(1)
                } else
                    VoterSnapshot.Detailed(listOf(user))
            }
        }
        publish(event.channelId, event.messageId)
    }

    @StoreThread
    fun publish(channelId: Long, messageId: Long) {
        subject.onNext(VoteEvent(channelId, messageId, snapshots[messageId]!!))
    }

    fun subscribe(channelId: Long, messageId: Long, onNext: (HashMap<Int, VoterSnapshot>) -> Unit): Subscription {
        onNext(snapshots[messageId]!!)
        return subject.subscribe {
            if (this.channelId == channelId && this.messageId == messageId)
                onNext(snapshots[messageId]!!)
        }
    }

    @StoreThread
    fun fetchDetails(channelId: Long, messageId: Long, answerId: Int) {
        val answers = snapshots[messageId]
        if (answers == null) {
            Logger("PollsStore").warn("Attempted to fetch details for uninitialised poll")
            return
        }

        val lastSnapshot = answers[answerId]
        if (lastSnapshot is VoterSnapshot.Loading)
            return
        if (lastSnapshot is VoterSnapshot.Detailed) {
            publish(channelId, messageId)
            return
        }

        val lastCount = lastSnapshot?.count ?: 0
        answers[answerId] = VoterSnapshot.Loading(lastCount)
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
                VoterSnapshot.Failed(lastCount)
            } else {
                VoterSnapshot.Detailed(data.map { CoreUser(it) })
            }
            StoreStream.getDispatcherYesThisIsIntentional().schedule {
                publish(channelId, messageId)
            }
        }
    }

    @StoreThread
    fun handleNewPoll(message: Message, force: Boolean = false) {
        val poll = message.poll
        if (poll == null)
            return logger.warn("Tried to handle message with no poll")

        if (snapshots.contains(message.id) && !force)
            return

        val target = HashMap<Int, VoterSnapshot>()
        poll.results?.answerCounts?.forEach {
            target[it.id] = VoterSnapshot.Lazy(it.count)
        }
        snapshots[message.id] = target
    }
}
