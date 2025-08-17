@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls

import com.aliucord.*
import com.aliucord.coreplugins.polls.PollsStore.fetchDetails
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.wrappers.messages.poll
import com.discord.api.message.poll.*
import com.discord.models.deserialization.gson.InboundGatewayGsonParser
import com.discord.stores.StoreStream
import com.google.gson.stream.JsonReader
import rx.Subscription
import rx.subjects.PublishSubject
import java.util.SortedSet
import com.discord.api.message.Message as ApiMessage
import com.discord.api.user.User as ApiUser
import com.discord.models.message.Message as ModelMessage

/**
 * A non-persistent store singleton that handles polls and changes to their votes.
 */
object PollsStore {
    private val logger = Logger("Store/Polls")
    private val dispatcher = StoreStream.getDispatcherYesThisIsIntentional()
    private val snapshots = HashMap<Long, HashMap<Int, VotesSnapshot>>()
    private val subject = PublishSubject.k0<VoteEvent>()

    /** The response payload used in [fetchDetails] */
    private data class ResponsePayload(val users: List<ApiUser>)

    /** A vote change event */
    private data class VoteEvent(
        val channelId: Long,
        val messageId: Long,
        val voteSnapshots: HashMap<Int, VotesSnapshot>?
    )

    /** A snapshot of vote details for an answer. */
    data class VotesSnapshot(
        /** Total vote count. */
        val count: Int,
        /**
         * A (lazy) set of voter user IDs. Count may be different from [count],
         * call [fetchDetails] to fetch more.
         */
        val voters: SortedSet<Long>,
        /** If the previous fetch request has failed. */
        val hasFailed: Boolean = false,
        /** Whether or not there is an ongoing fetch request. */
        val isLoading: Boolean = false,
    ) {
        /** Whether or not the current user has voted on this answer. */
        val meVoted = voters.any { it == StoreStream.getUsers().me.id }

        /** Whether or not [voters] is incomplete. */
        val isIncomplete = voters.size != count
    }

    /**
     * Handles a gateway event by also dispatching it to the store thread
     *
     * @param event Message poll vote update gateway event
     * @param isAdd Whether the event is VOTE_ADD (true) or VOTE_REMOVE (false)
     */
    fun dispatchGatewayEvent(event: MessagePollVoteEvent, isAdd: Boolean) {
        dispatcher.schedule {
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
        val answers = snapshots[event.messageId] ?: return
        val snapshot = answers[event.answerId]!!
        val count = snapshot.count + if (isAdd) +1 else -1
        val voters = when {
            // Do not add new voter to an incomplete snapshot; fetching more might skip users
            // with IDs less than the new voter
            // * however, we have special handling for self votes specifically (both in fetching
            //   and various other functions such as meVoted), so add anyway if its self voting
            snapshot.isIncomplete
                && event.userId > (snapshot.voters.lastOrNull() ?: 0)
                && event.userId != StoreStream.getUsers().me.id -> {
                snapshot.voters
            }
            isAdd -> {
                snapshot.voters.toSortedSet().apply { add(event.userId) }
            }
            else -> {
                snapshot.voters.toSortedSet().apply { remove(event.userId) }
            }
        }
        answers[event.answerId] = snapshot.copy(count = count, voters = voters)
        publish(event.channelId, event.messageId)
    }

    /**
     * Handles a message create/update event, should only be called from [StoreStream] methods.
     *
     * Must be run on the store thread.
     *
     * @param message New message object
     */
    fun handleMessageUpdate(message: ApiMessage) {
        handleMessageUpdate(message.g(), message.o(), message.poll)
    }

    /**
     * Handles a message create/update event, should only be called from [StoreStream] methods.
     *
     * Must be run on the store thread.
     *
     * @param message New message object
     */
    fun handleMessageUpdate(message: ModelMessage) {
        handleMessageUpdate(message.channelId, message.id, message.poll)
    }

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
        if (poll == null) return

        val target = HashMap<Int, VotesSnapshot>()
        poll.answers.forEach { answer ->
            val id = answer.answerId!!
            val answerCount = poll.results?.answerCounts?.find { it.id == id }
            val count = answerCount?.count ?: 0
            val voters = if (answerCount?.meVoted == true) {
                sortedSetOf(StoreStream.getUsers().me.id)
            } else {
                sortedSetOf()
            }
            target[id] = VotesSnapshot(count, voters)
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
        dispatcher.schedule {
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
    fun subscribeOnMain(channelId: Long, messageId: Long, onNext: (HashMap<Int, VotesSnapshot>?) -> Unit): Subscription {
        return subscribe(channelId, messageId) {
            Utils.mainThread.post { onNext(it) }
        }
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
            if (this.channelId == channelId && this.messageId == messageId) {
                onNext(snapshots[messageId])
            }
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

        val lastSnapshot = answers[answerId] ?: return
        if (lastSnapshot.isLoading || !lastSnapshot.isIncomplete) return

        val meId = StoreStream.getUsers().me.id
        // As part of me_voted, the me user may be added in out-of-order, therefore let's not consider them.
        val lastUser = lastSnapshot.voters.findLast { it != meId } ?: 0

        answers[answerId] = lastSnapshot.copy(isLoading = true, hasFailed = false)
        dispatchPublish(channelId, messageId)
        Utils.threadPool.execute {
            val request = runCatching {
                val res = Http.Request.newDiscordRNRequest(
                    "/channels/${channelId}/polls/${messageId}/answers/${answerId}?limit=100&after=${lastUser}"
                ).execute()
                res to InboundGatewayGsonParser.fromJson(JsonReader(res.stream().reader()), ResponsePayload::class.java).users
            }
            val (result, data) = request.getOrNull() ?: (null to listOf())
            answers[answerId] = if (result?.ok() != true) {
                logger.errorToast("Failed to fetch poll results")
                if (result != null) {
                    logger.error("${result.statusCode} ${result.statusMessage} ${result.text()}", null)
                } else {
                    logger.error(request.exceptionOrNull())
                }
                lastSnapshot.copy(isLoading = false, hasFailed = true)
            } else {
                dispatcher.schedule {
                    data.forEach { StoreStream.getUsers().handleUserUpdated(it) }
                }
                lastSnapshot.copy(isLoading = false, voters = lastSnapshot.voters.toSortedSet().apply { data.forEach { add(it.id) } })
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
    fun getResultFor(id: Long, finalised: Boolean): MessagePollResult? {
        return snapshots[id]?.let {
            MessagePollResult(finalised, it.map { (answerId, snapshot) ->
                MessagePollAnswerCount(answerId, snapshot.count, snapshot.meVoted)
            })
        }
    }
}
