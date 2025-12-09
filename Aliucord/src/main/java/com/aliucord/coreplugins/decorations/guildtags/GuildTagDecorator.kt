package com.aliucord.coreplugins.decorations.guildtags

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliucord.*
import com.aliucord.api.PatcherAPI
import com.aliucord.coreplugins.decorations.Decorator
import com.aliucord.patcher.*
import com.aliucord.utils.*
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.findViewById
import com.aliucord.wrappers.users.primaryGuild
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.databinding.WidgetChannelsListItemChannelPrivateBinding
import com.discord.stores.StoreLurking
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.error.Error
import com.discord.views.UsernameView
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItemPrivate
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.guilds.join.GuildJoinHelperKt
import com.discord.widgets.roles.RoleIconView
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.lytefast.flexinput.R

private val ChannelMembersListViewHolderMember.binding by accessField<WidgetChannelMembersListItemUserBinding>()
private val WidgetChannelsListAdapter.ItemChannelPrivate.binding by accessField<WidgetChannelsListItemChannelPrivateBinding>()
private val WidgetChatListAdapterItemMessage.itemName by accessField<TextView>()
private val WidgetChatListAdapterItemMessage.itemRoleIcon by accessField<RoleIconView>()
private val WidgetChatListAdapterItemMessage.replyLeadingViewsHolder by accessField<LinearLayout?>()

private const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT

private val logger = Logger("Decorations/GuildTag")

internal class GuildTagDecorator() : Decorator() {
    override fun patch(patcher: PatcherAPI) {
        patchReplyPreview(patcher)
        patchUsernameView(patcher)
    }

    companion object {
        private data class SetGuildIdentityPayload(
            val identityEnabled: Boolean,
            val identityGuildId: Long,
        )

        fun adoptTag(guildId: Long, callback: (() -> Unit)? = null) {
            Utils.threadPool.submit {
                try {
                    val req = Http.Request.newDiscordRNRequest("/users/@me/clan", "PUT")
                    req.executeWithJson(GsonUtils.gsonRestApi, SetGuildIdentityPayload(true, guildId))
                    callback?.invoke()
                } catch(e: Throwable) {
                    logger.errorToast("Failed to adopt guild tag", e)
                }
            }
        }

        fun joinGuild(context: Context, guildId: Long, callback: (() -> Unit)? = null) {
            Utils.threadPool.submit {
                try {
                    val sessionId: String = StoreLurking.`access$getSessionId$p`(StoreStream.getLurking())
                        ?: throw Throwable("Guild $guildId join failed due to missing session id")
                    GuildJoinHelperKt.`joinGuild$default`(
                        context,
                        guildId,
                        /* isLurker */ false,
                        sessionId,
                        /* directoryChannelId */ null,
                        /* contextProperties */ null,
                        /* errorClass */ GuildTagDecorator::class.java, // (only class name is used, for error reporting)
                        /* onSubscribe */ null,
                        /* onError */ { e: Any? ->
                            logger.errorToast("Failed to join guild")
                            logger.error((e as Error).toString(), null)
                        },
                        /* captchaPayload */ null,
                        /* onNext */ { _ -> callback?.invoke() },
                        0b01010110000,
                        null
                    )
                } catch(e: Throwable) {
                    logger.errorToast("Failed to join guild", e)
                }
            }
        }
    }

    // Needs to be done before, because reply preview width is dynamically calculated at the end (insane)
    private fun patchReplyPreview(patcher: PatcherAPI) {
        patcher.before<WidgetChatListAdapterItemMessage>(
            "configureReplyPreview",
            MessageEntry::class.java,
        ) { (_, entry: MessageEntry) ->
            val referencedAuthor = entry.message.referencedMessage?.e()
            itemView.findViewById<GuildTagView>(replyTagId)
                ?.configure(referencedAuthor?.primaryGuild)
        }
    }

    // Used in member list and profile header
    private fun patchUsernameView(patcher: PatcherAPI) {
        patcher.after<UsernameView>(Context::class.java, AttributeSet::class.java) {
            val binding = j
            val username = binding.c
            val extraTag = binding.b

            GuildTagView(context).addBetween(this, username, extraTag)
        }
    }

    // The tag view is already added in UsernameView
    override fun onMembersListConfigure(
        holder: ChannelMembersListViewHolderMember,
        item: ChannelMembersListAdapter.Item.Member,
        adapter: ChannelMembersListAdapter
    ) {
        val usernameView = holder.binding.f
        GuildTagView.findIn(usernameView)?.configure(StoreStream.getUsers().users[item.userId]?.primaryGuild)
    }

    // The tag view is already added in UsernameView
    override fun onProfileHeaderConfigure(
        view: UserProfileHeaderView,
        state: UserProfileHeaderViewModel.ViewState.Loaded
    ) {
        GuildTagView.findIn(view)?.run {
            setSize(15f)
            configure(state.user.primaryGuild)
        }
    }

    private val replyTagId = View.generateViewId()
    override fun onMessageInit(holder: WidgetChatListAdapterItemMessage, adapter: WidgetChatListAdapter) {
        val itemView = holder.itemView
        val headerLayout = itemView.findViewById<ConstraintLayout?>("chat_list_adapter_item_text_header")
            ?: return

        // Adds guild tag beside message author
        GuildTagView(itemView.context).addBetween(headerLayout, holder.itemName, holder.itemRoleIcon)

        val replyLeadingLayout = holder.replyLeadingViewsHolder
            ?: return

        // Adds guild tag beside referenced message (reply) author
        GuildTagView(itemView.context).addTo(replyLeadingLayout) {
            id = replyTagId
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginEnd = 4.dp
            }
            alpha = 0.7f
            setSize(10f)
        }
    }

    override fun onMessageConfigure(holder: WidgetChatListAdapterItemMessage, entry: MessageEntry) {
        GuildTagView.findIn(holder.itemView)?.configure(entry.message.author.primaryGuild)

        val referencedAuthor = entry.message.referencedMessage?.e()
        holder.itemView.findViewById<GuildTagView>(replyTagId)
            ?.configure(referencedAuthor?.primaryGuild)
    }

    override fun onDMsListInit(holder: WidgetChannelsListAdapter.ItemChannelPrivate, adapter: WidgetChannelsListAdapter) {
        val nameView = holder.binding.f
        val parent = nameView.parent as LinearLayout
        parent.gravity = Gravity.CENTER_VERTICAL
        GuildTagView(nameView.context).addTo(parent, 1) {
            setCardBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary))
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                marginStart = 6.dp
            }
        }
    }

    override fun onDMsListConfigure(holder: WidgetChannelsListAdapter.ItemChannelPrivate, item: ChannelListItemPrivate) {
        val user = ChannelUtils.getDMRecipient(item.channel)
        GuildTagView.findIn(holder.itemView)?.run {
            configure(user?.primaryGuild)
        }
    }
}
