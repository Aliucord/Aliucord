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
import com.aliucord.coreplugins.polls.MessagePollVoteEvent
import com.aliucord.coreplugins.polls.PollsStore
import com.aliucord.coreplugins.polls.chatview.PollChatEntry
import com.aliucord.coreplugins.polls.chatview.WidgetChatListAdapterItemPoll
import com.aliucord.coreplugins.polls.creation.PollCreateScreen
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.updater.ManagerBuild
import com.aliucord.utils.ReflectUtils
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
import com.discord.models.domain.ModelMessageDelete
import com.discord.models.member.GuildMember
import com.discord.models.user.MeUser
import com.discord.stores.*
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.permissions.ManageMessageContext
import com.discord.utilities.permissions.PermissionsContextsKt
import com.discord.utilities.spans.ClickableSpan
import com.discord.views.CheckedSetting
import com.discord.widgets.chat.input.*
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.discord.widgets.chat.list.adapter.*
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.chat.managereactions.ManageReactionsResultsAdapter
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

    // Show a confirmation dialog when ending polls
    private fun showPollConfirmationDialog(fragmentManager: FragmentManager, ctx: Context, msg: ModelMessage, onSuccess: () -> Unit) {
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

    override fun start(context: Context) {
        if (!ManagerBuild.hasInjector("2.2.0") || !ManagerBuild.hasPatches("1.2.0")) {
            logger.warn("Base app outdated, cannot enable Polls")
            return
        }
        patchStore()
        patchChatView()
        patchResultMessage(context)
        patchAttachmentSelector()
        patchChatListActions()
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun patchStore() {
        // Watch for poll vote gateway events
        GatewayAPI.onEvent<MessagePollVoteEvent>("MESSAGE_POLL_VOTE_ADD") { PollsStore.dispatchGatewayEvent(it, true) }
        GatewayAPI.onEvent<MessagePollVoteEvent>("MESSAGE_POLL_VOTE_REMOVE") { PollsStore.dispatchGatewayEvent(it, false) }

        // Patch store methods to manage them in our store
        patcher.patch(StoreStream::class.java.getDeclaredMethod("handleMessageCreate", ApiMessage::class.java))
        { (_, msg: ApiMessage) -> PollsStore.handleMessageUpdate(msg) }

        patcher.patch(StoreStream::class.java.getDeclaredMethod("handleMessageUpdate", ApiMessage::class.java))
        { (_, msg: ApiMessage) -> PollsStore.handleMessageUpdate(msg) }

        patcher.patch(StoreStream::class.java.getDeclaredMethod("handleMessageDelete", ModelMessageDelete::class.java))
        { (_, deleteModel: ModelMessageDelete) ->
            for (id in deleteModel.messageIds)
                PollsStore.handleMessageDelete(deleteModel.channelId, id)
        }

        patcher.patch(StoreStream::class.java.getDeclaredMethod("handleMessagesLoaded", StoreMessagesLoader.ChannelChunk::class.java))
        { (_, chunk: StoreMessagesLoader.ChannelChunk) ->
            for (msg in chunk.messages)
                PollsStore.handleMessageUpdate(msg)
        }


        // Patch ModelMessage to copy our polls from ApiMessage
        patcher.after<ModelMessage>(ApiMessage::class.java)
        { (_, apiMessage: ApiMessage) ->
            apiMessage.poll?.let { this.poll = it }
        }
        patcher.after<ModelMessage>("merge", ApiMessage::class.java)
        { (param, apiMessage: ApiMessage) ->
            val res = param.result as ModelMessage
            apiMessage.poll?.let { res.poll = it }
        }

        // When a poll ends, the message update payload may lose some results state. So we have to
        // patch this and retain the last known state. - Lava (lavadesu)
        patcher.before<StoreStream>("handleMessageUpdate", ApiMessage::class.java)
        { (_, msg: ApiMessage) ->
            val poll = msg.poll
            if (poll == null)
                return@before

            val oldResults = PollsStore.getResultFor(msg.o(), false)

            // If we don't have the message in cache, don't do anything.
            // The REST API sends me_voted correctly.
            if (oldResults == null)
                return@before

            // If new results is empty, retain last known results.
            // This happens during the first message update payload, when a poll is closed
            // but not finalised. Also recommended as per Discord's doc.
            if (poll.results == null) {
                msg.poll = poll.copy(results = oldResults)
                return@before
            }

            // Retain me_voted state from last known results.
            for (count in oldResults.answerCounts)
                if (count.meVoted)
                    poll.results!!.answerCounts.find { it.id == count.id }!!.meVoted = true
        }
    }

    private fun patchChatView() {
        // For PollChatAnswerView
        XposedBridge.makeClassInheritable(CheckedSetting::class.java)
        // For PollDetailsResultsAdapter
        XposedBridge.makeClassInheritable(ManageReactionsResultsAdapter::class.java)

        patcher.before<WidgetChatListAdapter>("onCreateViewHolder", ViewGroup::class.java, Int::class.javaPrimitiveType!!)
        { (param, _: ViewGroup, entryType: Int) ->
            if (entryType == PollChatEntry.POLL_ENTRY_TYPE)
                param.result = WidgetChatListAdapterItemPoll(this)
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
    }

    private fun patchResultMessage(context: Context) {
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
                val roleCtx = `$roleSubscriptionPurchaseContext` as `WidgetChatListAdapterItemSystemMessage$getSystemMessage$roleSubscriptionPurchaseContext$1`
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
                StoreStream.getMessagesLoader().jumpToMessage(msg.messageReference!!.a(), msg.messageReference!!.c())
                param.result = null
            }
        }
    }

    private fun patchAttachmentSelector() {
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
    }

    @SuppressLint("SetTextI18n")
    fun patchChatListActions() {
        // Allow deleting poll result messages
        patcher.patch(PermissionsContextsKt::class.java.getDeclaredMethod("isDeleteable", ModelMessage::class.java))
        { (param, msg: ModelMessage) ->
            if (msg.type == POLL_RESULT_MESSAGE_TYPE)
                param.result = true
        }

        // Other clients cannot edit poll messages to add content, despite it being allowed by the API
        // Here we also disable the functionality
        patcher.after<ManageMessageContext.Companion>(
            "from",
            ModelMessage::class.java,
            Long::class.javaObjectType,
            MeUser::class.java,
            Integer::class.java,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
        ) { (param, msg: ModelMessage) ->
            if (msg.poll != null)
                ReflectUtils.setField(param.result, "canEdit", false)
        }

        // Adds an "End poll now" button in message actions
        val endPollId = View.generateViewId()
        patcher.after<WidgetChatListActions>("configureUI", WidgetChatListActions.Model::class.java)
        { (_, model: WidgetChatListActions.Model) ->
            val layout = (requireView() as ViewGroup).getChildAt(0) as ViewGroup
            val msg = model.message!!

            if (msg.poll == null)
                return@after

            if (msg.author.id != model.me.id)
                return@after

            if (msg.poll!!.expiry!!.g() <= System.currentTimeMillis())
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
}

