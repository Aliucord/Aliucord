@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls

import com.aliucord.*
import com.aliucord.coreplugins.polls.PollsStore.fetchDetails
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.wrappers.messages.poll
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

/**
 * A non-persistent store singleton that handles polls and changes to their votes.
 */
object PollsStore {
    private val logger = Logger("Store/Polls")
    private val snapshots = HashMap<Long, HashMap<Int, VotesSnapshot>>()
    private val subject = PublishSubject.k0<VoteEvent?>()

    /** The response payload used in [fetchDetails] */
    private data class ResponsePayload(val users: List<ApiUser>)

    /** A vote change event */
    private data class VoteEvent(
        val channelId: Long,
        val messageId: Long,
        val voteSnapshots: HashMap<Int, VotesSnapshot>?
    )

    /** A snapshot of vote details for an answer */
    sealed class VotesSnapshot {
        /** Vote count. */
        abstract val count: Int
        /** Whether or not the current user has voted on this answer. */
        abstract val meVoted: Boolean

        /** When only vote counts are known. */
        data class Lazy(
            override val count: Int,
            override val meVoted: Boolean,
            /** If a previous [fetchDetails] request has failed. */
            val hasFailed: Boolean = false,
            /** If currently waiting for a response in [fetchDetails] */
            val isLoading: Boolean = false,
        ) : VotesSnapshot() {
            companion object {
                fun from(snapshot: VotesSnapshot?) = Lazy(snapshot?.count ?: 0, snapshot?.meVoted == true)
            }
        }

        /** Full details of each user that has voted for this answer. */
        data class Detailed(val voters: List<User> = listOf()) : VotesSnapshot() {
            override val count: Int
                get() = voters.size

            override val meVoted: Boolean
                get() {
                    val me = StoreStream.getUsers().me
                    return voters.any { it.id == me.id }
                }
        }
    }

    /**
     * Handles a gateway event by also dispatching it to the store thread
     *
     * @param event Message poll vote update gateway event
     * @param isAdd Whether the event is VOTE_ADD (true) or VOTE_REMOVE (false)
     */
    fun dispatchGatewayEvent(event: MessagePollVoteEvent, isAdd: Boolean) {
        StoreStream.getDispatcherYesThisIsIntentional().schedule {
            handleGatewayEvent(event, isAdd)
        }
    }

    /**
     * Handles a gateway event
     *
     * @param event Message poll vote update gateway event
     * @param isAdd Whether the event is VOTE_ADD (true) or VOTE_REMOVE (false)
     */
    private fun handleGatewayEvent(event: MessagePollVoteEvent, isAdd: Boolean) {
        val isSelfVote = event.userId == StoreStream.getUsers().me.id

        val answers = snapshots[event.messageId]
        if (answers == null)
            return

        val snapshot = answers[event.answerId]
        answers[event.answerId] = when (snapshot) {
            is VotesSnapshot.Lazy -> {
                val count = snapshot.count + (if (isAdd) +1 else -1)

                if (isSelfVote)
                    VotesSnapshot.Lazy(count, isAdd)
                else
                    snapshot.copy(count = count)
            }
            is VotesSnapshot.Detailed -> {
                val user = StoreStream.getUsers().users[event.userId]
                // TODO: Should we fetch the user, or just downgrade?
                if (user == null) {
                    logger.warn("New voter is not in store, downgrading")
                    VotesSnapshot.Lazy(snapshot.voters.size + 1, isSelfVote)
                } else {
                    val newVoters = if (isAdd)
                        snapshot.voters + user
                    else
                        snapshot.voters - user
                    VotesSnapshot.Detailed(newVoters)
                }
            }
            else -> {
                val user = StoreStream.getUsers().users[event.userId]
                if (user == null) {
                    logger.warn("New voter is not in store, downgrading")
                    VotesSnapshot.Lazy(1, isSelfVote)
                } else
                    VotesSnapshot.Detailed(listOf(user))
            }
        }
        publish(event.channelId, event.messageId)
    }

    /**
     * Handles a message create/update event, should only be called from [StoreStream] methods.
     *
     * Must be run on the store thread.
     *
     * @param message New message object
     */
    fun handleMessageUpdate(message: ApiMessage) =
        handleMessageUpdate(message.g(), message.o(), message.poll)

    /**
     * Handles a message create/update event, should only be called from [StoreStream] methods.
     *
     * Must be run on the store thread.
     *
     * @param message New message object
     */
    fun handleMessageUpdate(message: ModelMessage) =
        handleMessageUpdate(message.channelId, message.id, message.poll)

    /**
     * Handles a message create/update event.
     *
     * Must be run on the store thread.
     *
     * @param channelId Channel ID message was sent in
     * @param messageId Message ID
     * @param poll Message poll object
     */
    private fun handleMessageUpdate(channelId: Long, messageId: Long, poll: MessagePoll?) {
        if (poll == null)
            return

        val target = HashMap<Int, VotesSnapshot>()
        poll.results?.answerCounts?.forEach {
            target[it.id] = VotesSnapshot.Lazy(it.count, it.meVoted)
        }
        snapshots[messageId] = target
        publish(channelId, messageId)
    }

    /**
     * Handles a message delete event, should only be called from [StoreStream] methods.
     *
     * Must be run on the store thread.
     *
     * @param channelId Channel ID message was sent in
     * @param messageId Message ID
     */
    fun handleMessageDelete(channelId: Long, messageId: Long) {
        snapshots.remove(messageId)
        publish(channelId, messageId)
    }

    /**
     * Publishes a [VoteEvent] to the subject.
     *
     * Must be run on the store thread.
     *
     * @param channelId Channel ID message was sent in
     * @param messageId Message ID
     */
    private fun publish(channelId: Long, messageId: Long) {
        subject.onNext(VoteEvent(channelId, messageId, snapshots[messageId]))
    }

    /**
     * Dispatches a publication of [VoteEvent] on the store thread.
     *
     * @param channelId Channel ID message was sent in
     * @param messageId Message ID
     */
    private fun dispatchPublish(channelId: Long, messageId: Long) {
        StoreStream.getDispatcherYesThisIsIntentional().schedule {
            publish(channelId, messageId)
        }
    }

    /**
     * Subscribes to poll changes, and run the handler on the main UI thread.
     *
     * @param channelId Channel ID of message
     * @param messageId Message ID to subscribe to
     * @param onNext Event handler, will run on the main thread
     */
    fun subscribeOnMain(channelId: Long, messageId: Long, onNext: (HashMap<Int, VotesSnapshot>?) -> Unit) =
        subscribe(channelId, messageId) {
            Utils.mainThread.post { onNext(it) }
        }

    /**
     * Subscribes to poll changes.
     *
     * @param channelId Channel ID of message
     * @param messageId Message ID to subscribe to
     * @param onNext Event handler
     */
    fun subscribe(channelId: Long, messageId: Long, onNext: (HashMap<Int, VotesSnapshot>?) -> Unit): Subscription {
        snapshots[messageId]?.let { onNext(it) }
        return subject.subscribe {
            if (this.channelId == channelId && this.messageId == messageId)
                onNext(snapshots[messageId])
        }
    }

    /**
     * Fetches detailed votes for a poll.
     *
     * @param channelId Channel ID of message
     * @param messageId Message ID
     * @param answerId Answer ID to fetch detailed votes for
     */
    fun fetchDetails(channelId: Long, messageId: Long, answerId: Int) {
        val answers = snapshots[messageId]
        if (answers == null) {
            Logger("PollsStore").warn("Attempted to fetch details for unknown poll $channelId/$messageId")
            return
        }

        val lastSnapshot = answers[answerId] as? VotesSnapshot.Lazy
        if (lastSnapshot == null)
            return
        if (lastSnapshot.isLoading)
            return

        answers[answerId] = lastSnapshot.copy(isLoading = true)
        dispatchPublish(channelId, messageId)
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
                lastSnapshot.copy(isLoading = false, hasFailed = true)
            } else {
                VotesSnapshot.Detailed(data.map { CoreUser(it) })
            }
            dispatchPublish(channelId, messageId)
        }
    }

    /**
     * Gets the stored votes for a message as [MessagePollResult] form.
     *
     * @param id Message id
     * @param finalised Whether or not the poll is finalised
     */
    fun getResultFor(id: Long, finalised: Boolean) = snapshots[id]?.let {
        MessagePollResult(finalised, it.map { (answerId, snapshot) ->
            MessagePollAnswerCount(answerId, snapshot.count, snapshot.meVoted)
        })
    }
}
