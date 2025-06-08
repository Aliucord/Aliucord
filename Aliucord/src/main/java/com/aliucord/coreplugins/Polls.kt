package com.aliucord.coreplugins

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import com.aliucord.api.GatewayAPI
import com.aliucord.coreplugins.polls.*
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.api.message.poll.*
import com.discord.stores.StoreStream
import com.discord.views.CheckedSetting
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.entries.ChatListEntry
import de.robv.android.xposed.XposedBridge
import com.discord.api.message.Message as ApiMessage
import com.discord.models.message.Message as ModelMessage

internal class Polls : CorePlugin(Manifest("Polls")) {
    override val isHidden: Boolean = true
    override val isRequired: Boolean = true

    private val apiMsgPollField = ApiMessage::class.java.getDeclaredField("poll")
    private val modelMsgPollField = ModelMessage::class.java.getDeclaredField("poll")

    @Synchronized
    fun handleVoteChange(event: MessagePollVoteEvent, isAdd: Boolean) {
        val store = StoreStream.getMessages()
        @Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")
        val meId = StoreStream.getUsers().me.id

        val msg = store.getMessage(event.channelId, event.messageId)
        if (msg == null)
            return

        val poll = modelMsgPollField.get(msg) as MessagePoll?
        if (poll == null) {
            logger.error("POLL_VOTE gateway event received for message that is not a poll", null)
            return
        }

        val results = poll.results?.copy() ?: MessagePollResult(false, listOf())
        val counts = results.answerCounts.toMutableList()

        var targetCount = counts.find { it.id == event.answerId }
            ?: MessagePollAnswerCount(event.answerId, 0, false).apply { counts.add(this) }
        if (isAdd)
            targetCount.count += 1
        else
            targetCount.count -= 1

        if (event.userId == meId)
            targetCount.meVoted = isAdd

        val newMsg = msg.synthesizeApiMessage()
        apiMsgPollField.set(newMsg, poll.copy(results = results.copy(answerCounts = counts)))

        store.handleMessageUpdate(newMsg)
    }

    @SuppressLint("SetTextI18n")
    override fun start(context: Context) {
        // For PollAnswerView
        XposedBridge.makeClassInheritable(CheckedSetting::class.java)

        patcher.before<WidgetChatListAdapter>("onCreateViewHolder", ViewGroup::class.java, Int::class.javaPrimitiveType!!) { call ->
            val entryType = call.args[1] as Int

            if (entryType == PollChatEntry.POLL_ENTRY_TYPE) {
                call.result = PollViewHolder(this)
            }
        }

        GatewayAPI.onEvent<MessagePollVoteEvent>("MESSAGE_POLL_VOTE_ADD") { handleVoteChange(it, true) }
        GatewayAPI.onEvent<MessagePollVoteEvent>("MESSAGE_POLL_VOTE_REMOVE") { handleVoteChange(it, false) }

        patcher.patch(ModelMessage::class.java.getDeclaredConstructor(ApiMessage::class.java), Hook { callFrame ->
            val poll = apiMsgPollField.get(callFrame.args[0]) as MessagePoll?
            modelMsgPollField.set(callFrame.thisObject, poll)
        })
        patcher.patch(ModelMessage::class.java.getDeclaredMethod("merge", ApiMessage::class.java), Hook { callFrame ->
            val poll = apiMsgPollField.get(callFrame.args[0]) as MessagePoll?
            if (poll != null)
                modelMsgPollField.set(callFrame.result, poll)
        })

        // createEmbedEntries *might* be a better candidate to add our entries, but also this
        // method has a far less complicated signature, so im using it - Lava (lavadesu)
        patcher.after<ChatListEntry.Companion>(
            "createStickerEntries",
            ModelMessage::class.java,
        ) { call ->
            val message = call.args[0] as ModelMessage
            val poll = modelMsgPollField.get(message) as MessagePoll?
            if (poll == null)
                return@after

            @Suppress("UNCHECKED_CAST")
            val res = (call.result as List<ChatListEntry>).toMutableList()
            res.add(PollChatEntry(poll, message))
            call.result = res
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}

