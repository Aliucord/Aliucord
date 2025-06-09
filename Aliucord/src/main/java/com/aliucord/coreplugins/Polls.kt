package com.aliucord.coreplugins

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import com.aliucord.api.GatewayAPI
import com.aliucord.coreplugins.polls.MessagePollVoteEvent
import com.aliucord.coreplugins.polls.PollChatEntry
import com.aliucord.coreplugins.polls.PollViewHolder
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.after
import com.aliucord.patcher.before
import com.aliucord.wrappers.embeds.FieldWrapper.Companion.name
import com.aliucord.wrappers.embeds.FieldWrapper.Companion.value
import com.aliucord.wrappers.embeds.MessageEmbedWrapper.Companion.rawFields
import com.aliucord.wrappers.messages.MessageWrapper.Companion.poll
import com.discord.api.channel.Channel
import com.discord.api.message.poll.MessagePollAnswerCount
import com.discord.api.message.poll.MessagePollResult
import com.discord.models.member.GuildMember
import com.discord.stores.StoreMessageState
import com.discord.stores.StoreStream
import com.discord.utilities.spans.ClickableSpan
import com.discord.views.CheckedSetting
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemSystemMessage
import com.discord.widgets.chat.list.adapter.`WidgetChatListAdapterItemSystemMessage$getSystemMessage$1`
import com.discord.widgets.chat.list.adapter.`WidgetChatListAdapterItemSystemMessage$getSystemMessage$roleSubscriptionPurchaseContext$1`
import com.discord.widgets.chat.list.adapter.`WidgetChatListAdapterItemSystemMessage$getSystemMessage$usernameRenderContext$1`
import com.discord.widgets.chat.list.adapter.`WidgetChatListAdapterItemSystemMessage$onConfigure$1`
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.lytefast.flexinput.R
import de.robv.android.xposed.XposedBridge
import kotlin.math.roundToInt
import com.discord.api.message.Message as ApiMessage
import com.discord.models.message.Message as ModelMessage

internal class Polls : CorePlugin(Manifest("Polls")) {
    override val isHidden: Boolean = true
    override val isRequired: Boolean = true

    companion object {
        const val POLL_RESULT_MESSAGE_TYPE = 46
    }

    @Synchronized
    fun handleVoteChange(event: MessagePollVoteEvent, isAdd: Boolean) {
        val store = StoreStream.getMessages()
        @Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")
        val meId = StoreStream.getUsers().me.id

        val msg = store.getMessage(event.channelId, event.messageId)
        if (msg == null)
            return

        val poll = msg.poll
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
        newMsg.poll = poll.copy(results = results.copy(answerCounts = counts))

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

        // Patch ModelMessage to copy our polls from ApiMessage
        patcher.after<ModelMessage>(ApiMessage::class.java) { callFrame ->
            val apiMessage = callFrame.args[0] as ApiMessage
            if (apiMessage.poll != null)
                poll = apiMessage.poll?.copy()
        }
        patcher.after<ModelMessage>("merge", ApiMessage::class.java) { callFrame ->
            val apiMessage = callFrame.args[0] as ApiMessage
            val res = callFrame.result as ModelMessage
            if (apiMessage.poll != null)
                res.poll = apiMessage.poll?.copy()
        }

        /**
         * So apparently when a poll ends, in the message update payload, all of me_voted are
         * cleared to false. Therefore, we need to patch the event handler to find the last model
         * and then copy over the voted status here.
         *
         * BUT ALSO, when a poll ends, there is actually TWO events that get sent: the first one
         * updates the expiry date to close the poll; however, this data is actually missing the
         * results because they aren't finalised yet. It's only the second event (poll finalised)
         * that the results data is included, but the me_voted is cleared with it.
         *
         * So we also have to copy the results over during the first event (which is recommended
         * per Discord's doc), and then in the second event we use that to keep me_voted data.
         *
         * - Lava (lavadesu)
         */
        patcher.before<StoreStream>("handleMessageUpdate", ApiMessage::class.java) { callFrame ->
            val msg = callFrame.args[0] as ApiMessage
            val poll = msg.poll
            if (poll == null)
                return@before

            val oldPoll = StoreStream.getMessages().getMessage(msg.g(), msg.o())?.poll

            // If we don't have the message in cache, don't do anything.
            // The REST API sends me_voted correctly.
            if (oldPoll == null)
                return@before

            // First (unfinalised) event: Retain old results
            if (poll.results == null) {
                msg.poll = poll.copy(results = oldPoll.results)
                return@before
            }

            // Second (finalised) event: Retain me_voted
            for (count in oldPoll.results!!.answerCounts)
                if (count.meVoted)
                    poll.results!!.answerCounts.find { it.id == count.id }!!.meVoted = true
        }

        patcher.after<ChatListEntry.Companion>(
            "createEmbedEntries",
            ModelMessage::class.java,
            StoreMessageState.State::class.java,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Channel::class.java,
            GuildMember::class.java,
            Map::class.java,
            Map::class.java,
        ) { call ->
            val message = call.args[0] as ModelMessage
            val poll = message.poll
            if (poll == null)
                return@after

            @Suppress("UNCHECKED_CAST")
            val res = (call.result as List<ChatListEntry>).toMutableList()
            res.add(0, PollChatEntry(poll, message))
            call.result = res
        }

        // Patch poll result message icon
        patcher.before<WidgetChatListAdapterItemSystemMessage>(
            "getIcon",
            ModelMessage::class.java
        ) { call ->
            val msg = call.args[0] as ModelMessage
            if (msg.type == POLL_RESULT_MESSAGE_TYPE) // POLL_RESULT
                call.result = R.e.ic_sort_white_24dp
        }
        // Patch poll result message content
        patcher.before<`WidgetChatListAdapterItemSystemMessage$getSystemMessage$1`>(
            "invoke",
            Context::class.java
        ) { call ->
            val msg = `$this_getSystemMessage`
            if (msg.type == POLL_RESULT_MESSAGE_TYPE) {
                val renderCtx = `$usernameRenderContext` as `WidgetChatListAdapterItemSystemMessage$getSystemMessage$usernameRenderContext$1`
                val color = renderCtx.`$authorRoleColor`

                var pollQuestionText = ""
                var victorAnswerVotes = 0
                var totalVotes = 0
                var victorAnswerId: Int? = null
                var victorAnswerText: String? = null
                msg.embeds.getOrNull(0)?.rawFields?.forEach {
                    when (it.name) {
                        "poll_question_text" -> pollQuestionText = it.value
                        "victor_answer_votes" -> victorAnswerVotes = it.value.toInt()
                        "total_votes" -> totalVotes = it.value.toInt()
                        "victor_answer_id" -> victorAnswerId = it.value.toInt()
                        "victor_answer_text" -> victorAnswerText = it.value
                    }
                } ?: return@before logger.error("Tried to render poll result, but there was no embed?", null)

                val span = SpannableStringBuilder()
                val authorSpan = ClickableSpan(color, false, null) {
                    val roleCtx = `$roleSubscriptionPurchaseContext` as `WidgetChatListAdapterItemSystemMessage$getSystemMessage$roleSubscriptionPurchaseContext$1`;
                    @Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")
                    roleCtx.`this$0`.adapter.eventHandler.onMessageAuthorAvatarClicked(msg, StoreStream.getGuildSelected().selectedGuildId)
                }
                span.append(`$authorName`, authorSpan, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.append("'s poll ")
                span.append(pollQuestionText, StyleSpan(Typeface.BOLD), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.append(" has closed! ")

                if (totalVotes == 0)
                    totalVotes = 1 // Prevent division by 0
                val percent = (victorAnswerVotes.toDouble() * 100 / totalVotes).roundToInt()
                if (victorAnswerVotes == 0)
                    span.append("There were no votes :(")
                else if (victorAnswerText == null)
                    span.append("The result was a draw (${percent}%).")
                else {
                    span.append("The winner was ")
                    span.append(victorAnswerText, StyleSpan(Typeface.BOLD), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                    span.append(" (${percent}%).")
                }

                call.result = span
            }
        }
        // Patch message onClick to jump to poll from poll result
        patcher.before<`WidgetChatListAdapterItemSystemMessage$onConfigure$1`>("onClick", View::class.java) { call ->
            @Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")
            val msg = `$message`
            if (msg.type == POLL_RESULT_MESSAGE_TYPE) {
                StoreStream.getMessagesLoader().jumpToMessage(msg.messageReference!!.a(), msg.messageReference!!.c());
                call.result = null
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}

