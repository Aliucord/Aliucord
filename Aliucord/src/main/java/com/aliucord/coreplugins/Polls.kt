@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")
package com.aliucord.coreplugins

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.api.GatewayAPI
import com.aliucord.api.rn.user.RNUser
import com.aliucord.coreplugins.polls.*
import com.aliucord.coreplugins.polls.creation.PollCreateScreen
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.updater.ManagerBuild
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.aliucord.wrappers.ChannelWrapper.Companion.isDM
import com.aliucord.wrappers.ChannelWrapper.Companion.name
import com.aliucord.wrappers.ChannelWrapper.Companion.recipients
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
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.spans.ClickableSpan
import com.discord.views.CheckedSetting
import com.discord.widgets.chat.input.*
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.discord.widgets.chat.list.adapter.*
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.notice.WidgetNoticeDialog
import com.google.android.material.tabs.TabLayout
import com.lytefast.flexinput.R
import com.lytefast.flexinput.fragment.FlexInputFragment
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

    // Handle vote changes from the gateway
    @Synchronized
    fun handleVoteChange(event: MessagePollVoteEvent, isAdd: Boolean) {
        val store = StoreStream.getMessages()
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

    // Show a confirmation dialog when ending polls
    fun showPollConfirmationDialog(fragmentManager: FragmentManager, ctx: Context, msg: ModelMessage, onSuccess: () -> Unit) {
        WidgetNoticeDialog.show(
            fragmentManager,
            "End poll now?",
            "This will close the poll immediately and reveal the results.",
            ctx.getString(R.h.okay),
            ctx.getString(R.h.cancel),
            mapOf(
                Utils.getResId("notice_ok", "id") to {
                    Utils.threadPool.execute {
                        val req = Http.Request.newDiscordRNRequest(
                            "/channels/${msg.channelId}/polls/${msg.id}/expire",
                            "POST"
                        ).setRequestTimeout(10000).execute()
                        if (!req.ok()) {
                            logger.errorToast("Failed to end poll")
                            logger.error("${req.statusCode} ${req.statusMessage} ${req.text()}", null)
                        }
                    }
                    onSuccess()
                }
            ),
            null,
            null,
            null,
            R.b.notice_theme_positive_red,
            false,
            null,
            0,
            null
        )
    }

    @SuppressLint("SetTextI18n")
    override fun start(context: Context) {
        if (!ManagerBuild.hasInjector("2.2.0") || !ManagerBuild.hasPatches("1.2.0")) {
            logger.warn("Base app outdated, cannot enable Polls");
            return;
        }

        // For PollAnswerView
        XposedBridge.makeClassInheritable(CheckedSetting::class.java)

        patcher.before<WidgetChatListAdapter>("onCreateViewHolder", ViewGroup::class.java, Int::class.javaPrimitiveType!!)
        { (param, _: ViewGroup, entryType: Int) ->
            if (entryType == PollChatEntry.POLL_ENTRY_TYPE)
                param.result = WidgetChatListAdapterItemPoll(this)
        }

        // Watch for poll vote gateway events
        GatewayAPI.onEvent<MessagePollVoteEvent>("MESSAGE_POLL_VOTE_ADD") { handleVoteChange(it, true) }
        GatewayAPI.onEvent<MessagePollVoteEvent>("MESSAGE_POLL_VOTE_REMOVE") { handleVoteChange(it, false) }

        // Patch ModelMessage to copy our polls from ApiMessage
        patcher.after<ModelMessage>(ApiMessage::class.java)
        { (_, apiMessage: ApiMessage) ->
            if (apiMessage.poll != null)
                poll = apiMessage.poll
        }
        patcher.after<ModelMessage>("merge", ApiMessage::class.java)
        { (param, apiMessage: ApiMessage) ->
            val res = param.result as ModelMessage
            if (apiMessage.poll != null)
                res.poll = apiMessage.poll
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
        patcher.before<StoreStream>("handleMessageUpdate", ApiMessage::class.java)
        { (_, msg: ApiMessage) ->
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

        // Patch to attach our poll entry into the chat; we do this before embeds like other clients
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
        ) { (param, msg: ModelMessage) ->
            val poll = msg.poll
            if (poll == null)
                return@after

            @Suppress("UNCHECKED_CAST")
            val res = (param.result as List<ChatListEntry>).toMutableList()
            res.add(0, PollChatEntry(poll, msg))
            param.result = res
        }

        // Patch poll result message icon
        patcher.after<WidgetChatListAdapterItemSystemMessage>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: MessageEntry) ->
            if (entry.message.type != POLL_RESULT_MESSAGE_TYPE)
                return@after

            val imageView = WidgetChatListAdapterItemSystemMessage.`access$getBinding$p`(this).f
            val drawable = ContextCompat.getDrawable(context, R.e.ic_sort_white_24dp)?.apply {
                mutate()
                Utils.tintToTheme(this)
            }
            drawable?.let { imageView.setImageDrawable(it) }
        }
        // Patch poll result message content
        patcher.before<`WidgetChatListAdapterItemSystemMessage$getSystemMessage$1`>(
            "invoke",
            Context::class.java
        ) { param ->
            val msg = `$this_getSystemMessage`
            if (msg.type != POLL_RESULT_MESSAGE_TYPE)
                return@before

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

            param.result = span
        }
        // Patch message onClick to jump to poll from poll result
        patcher.before<`WidgetChatListAdapterItemSystemMessage$onConfigure$1`>("onClick", View::class.java)
        { param ->
            val msg = `$message`
            if (msg.type == POLL_RESULT_MESSAGE_TYPE) {
                StoreStream.getMessagesLoader().jumpToMessage(msg.messageReference!!.a(), msg.messageReference!!.c());
                param.result = null
            }
        }

        // Patch the input attachments to add a button to create polls
        val pollStringId = View.generateViewId()
        patcher.after<`WidgetChatInputAttachments$configureFlexInputContentPages$1`>("invoke") {
            val flexInputFragment = WidgetChatInputAttachments.`access$getFlexInputFragment$p`(this.`this$0`)
            val ctx = flexInputFragment.requireContext()
            val pages = flexInputFragment.r.toMutableList()
            val page = `WidgetChatInputAttachments$configureFlexInputContentPages$1$page$1`(ctx, R.e.ic_sort_white_24dp, pollStringId)
            @Suppress("CAST_NEVER_SUCCEEDS")
            pages.add(page as b.b.a.d.d.a) // I don't know why but IntelliJ complains that page is not the right type
            flexInputFragment.r = pages.toTypedArray()
        }
        patcher.before<TabLayout.Tab>("setContentDescription", Int::class.javaPrimitiveType!!)
        { (param, id: Int) ->
            if (id == pollStringId) {
                tag = "poll"
                param.result = setContentDescription("Create Poll")
            }
        }
        patcher.after<TabLayout.Tab>("setIcon", Int::class.javaPrimitiveType!!)
        { (_, id: Int) ->
            if (id == R.e.ic_sort_white_24dp) {
                val color = ColorCompat.getThemedColor(view, R.b.flexInputIconColor)
                icon?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }
        }

        patcher.before<b.b.a.a.b>("onTabSelected", TabLayout.Tab::class.java)
        { (param, tab: TabLayout.Tab) ->
            if (tab.tag != "poll")
                return@before

            val ctx = this.a.requireContext()
            val parentFragment = this.a.parentFragment as FlexInputFragment
            parentFragment.s.onContentDialogDismissed(false)
            param.result = null
            val channel = StoreStream.getChannelsSelected().selectedChannel
            val name = if (channel.isDM()) {
                channel.recipients?.firstOrNull()?.let {
                    if (it is RNUser)
                        it.globalName
                    else
                        it.username
                } ?: "unknown user"
            } else {
                "#" + channel.name
            }
            PollCreateScreen.launch(ctx, name, channel.id)
        }

        // Adds an "End poll now" button in message actions
        val endPollId = View.generateViewId()
        patcher.after<WidgetChatListActions>("configureUI", WidgetChatListActions.Model::class.java)
        { (_, model: WidgetChatListActions.Model) ->
            val layout = (requireView() as ViewGroup).getChildAt(0) as ViewGroup
            val msg = model.message!!

            if (msg.poll == null || msg.author.id != StoreStream.getUsers().me.id)
                return@after

            if (layout.findViewById<View>(endPollId) != null)
                return@after

            val replyView =
                layout.findViewById<View>(Utils.getResId("dialog_chat_actions_edit", "id")) ?: return@after
            val idx = layout.indexOfChild(replyView)

            TextView(layout.context, null, 0, R.i.UiKit_Settings_Item_Icon).addTo(layout, idx) {
                id = endPollId
                text = "End poll now"
                setOnClickListener {
                    showPollConfirmationDialog(childFragmentManager, requireContext(), msg) {
                        dismiss()
                    }
                }
                ContextCompat.getDrawable(layout.context, R.e.ic_sort_white_24dp)?.run {
                    mutate()
                    setTint(ColorCompat.getThemedColor(layout.context, R.b.colorInteractiveNormal))
                    setCompoundDrawablesRelativeWithIntrinsicBounds(this, null, null, null)
                }
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}

