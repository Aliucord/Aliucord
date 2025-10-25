package com.aliucord.coreplugins.decorations.guildtags

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.fragment.app.FragmentActivity
import com.aliucord.*
import com.aliucord.api.PatcherAPI
import com.aliucord.coreplugins.decorations.DecorationsSettings
import com.aliucord.patcher.*
import com.aliucord.utils.*
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.findViewById
import com.aliucord.wrappers.users.primaryGuild
import com.discord.api.user.PrimaryGuild
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.stores.*
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.error.Error
import com.discord.utilities.icon.IconUtils
import com.discord.views.UsernameView
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem
import com.discord.widgets.channels.list.items.ChannelListItemPrivate
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.guilds.join.GuildJoinHelperKt
import com.discord.widgets.roles.RoleIconView
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.facebook.drawee.span.SimpleDraweeSpanTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.lytefast.flexinput.R

private val logger = Logger("Decorations/GuildTags")
private val guildTagViewId = View.generateViewId()
private val ChannelMembersListViewHolderMember.binding by accessField<WidgetChannelMembersListItemUserBinding>()

private data class SetGuildIdentityPayload(
    val identityEnabled: Boolean,
    val identityGuildId: Long,
)

private class GuildTagView(ctx: Context) : CardView(ctx) {
    private lateinit var badge: SimpleDraweeView
    private lateinit var text: TextView

    fun setSize(dp: Float) {
        val badgeSize = (Utils.appContext.resources.displayMetrics.scaledDensity * (dp + 2) + 0.5f).toInt()
        badge.layoutParams = LinearLayout.LayoutParams(badgeSize, badgeSize)
        text.textSize = dp
    }

    companion object {
        fun findIn(view: View): GuildTagView? = view.findViewById(guildTagViewId)
    }

    init {
        id = guildTagViewId
        visibility = View.GONE
        radius = 4.dp.toFloat()
        elevation = 0f
        setCardBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundSecondary))
        setContentPadding(4.dp, 0.5f.dp, 4.dp, 0.5f.dp)

        LinearLayout(context).addTo(this) {
            layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            badge = SimpleDraweeView(context).addTo(this)
            text = TextView(context, null, 0, R.i.UiKit_TextView).addTo(this) {
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    leftMargin = 2.dp
                }
                setTypeface(typeface, Typeface.BOLD)
                isSingleLine = true
            }
        }

        setSize(12f)
    }

    /* Adds the tag to a constraint layout between two views */
    fun addBetween(parent: ConstraintLayout, left: View, right: View) {
        this.addTo(parent) {
            left.layoutParams = (left.layoutParams as ConstraintLayout.LayoutParams).apply {
                endToStart = id
            }
            right.layoutParams = (right.layoutParams as ConstraintLayout.LayoutParams).apply {
                startToEnd = id
            }

            layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply {
                marginStart = 4.dp
                verticalBias = 0.5f

                topToTop = PARENT_ID
                bottomToBottom = PARENT_ID
                startToEnd = left.id
                endToStart = right.id
            }
        }
    }

    fun configure(data: PrimaryGuild?) {
        setOnClickListener(null)
        visibility = GONE
        if (data == null) return

        val enabled = data.identityEnabled ?: return
        if (!enabled) return

        val tag = data.tag ?: return
        val guildId = data.identityGuildId ?: return
        val badgeHash = data.badge ?: return

        visibility = VISIBLE
        text.text = tag
        IconUtils.setIcon(
            /* view */ badge,
            /* url */ "https://cdn.discordapp.com/guild-tag-badges/${guildId}/${badgeHash}.png",
        )
        setOnClickListener {
            val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager
            if (fragmentManager == null) {
                logger.warn("Could not find fragment manager to show guild profile sheet")
                return@setOnClickListener
            }
            GuildProfileSheet.show(fragmentManager, guildId)
        }
    }
}

internal object GuildTags {
    fun patch(patcher: PatcherAPI) {
        if (!DecorationsSettings.enableGuildTags) return

        patchUsernameView(patcher)
        patchMemberList(patcher)
        patchProfileHeader(patcher)
        patchMessageAuthor(patcher)
        patchDMsList(patcher)
    }

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
                val sessionId: String? = StoreLurking.`access$getSessionId$p`(StoreStream.getLurking())
                if (sessionId == null) {
                    throw Throwable("Guild $guildId join failed due to missing session id")
                }
                GuildJoinHelperKt.`joinGuild$default`(
                    context,
                    guildId,
                    /* isLurker */ false,
                    sessionId,
                    /* directoryChannelId */ null,
                    /* contextProperties */ null,
                    /* errorClass */ GuildTags::class.java, // (only class name is used, for error reporting)
                    /* onSubscribe */ null,
                    /* onError */ { e: Any? ->
                        logger.errorToast("Failed to join guild")
                        logger.error((e as Error).toString(), null)
                    },
                    /* captchaPayload */ null,
                    /* onNext */ { _ -> callback?.invoke() },
                    0b01110110000,
                    null
                )
            } catch(e: Throwable) {
                logger.errorToast("Failed to join guild", e)
            }
        }
    }

    // Used in member list and profile header
    private fun patchUsernameView(patcher: PatcherAPI) {
        patcher.after<UsernameView>(Context::class.java, AttributeSet::class.java) {
            val username = findViewById<SimpleDraweeSpanTextView>("username_text")
            val extraTag = findViewById<TextView>("username_tag")

            GuildTagView(context).addBetween(this, username, extraTag)
        }
    }

    private fun patchMemberList(patcher: PatcherAPI) {
        // Patches the method that configures the member; the tag view is added in UsernameView
        patcher.after<ChannelMembersListViewHolderMember>(
            "bind",
            ChannelMembersListAdapter.Item.Member::class.java,
            Function0::class.java,
        ) { (_, member: ChannelMembersListAdapter.Item.Member) ->
            val usernameView = binding.f
            GuildTagView.findIn(usernameView)?.configure(StoreStream.getUsers().users[member.userId]?.primaryGuild)
        }
    }

    private fun patchProfileHeader(patcher: PatcherAPI) {
        // Patches the method that configures the header; the tag view is added in UsernameView
        patcher.after<UserProfileHeaderView>(
            "updateViewState",
            UserProfileHeaderViewModel.ViewState.Loaded::class.java,
        ) { (_, state: UserProfileHeaderViewModel.ViewState.Loaded) ->
            GuildTagView.findIn(this)?.run {
                setSize(15f)
                configure(state.user.primaryGuild)
            }
        }
    }

    private val replyTagId = View.generateViewId()
    private fun patchMessageAuthor(patcher: PatcherAPI) {
        // Creates the guild tag view for message author and reply preview
        patcher.after<WidgetChatListAdapterItemMessage>(Int::class.javaPrimitiveType!!, WidgetChatListAdapter::class.java) {
            val headerLayout = itemView.findViewById<ConstraintLayout?>("chat_list_adapter_item_text_header")
                ?: return@after

            val username = headerLayout.findViewById<TextView>("chat_list_adapter_item_text_name")
            val roleIcon = headerLayout.findViewById<RoleIconView>("chat_list_adapter_item_text_role_icon")

            GuildTagView(itemView.context).addBetween(headerLayout, username, roleIcon)

            val replyLeadingLayout = itemView.findViewById<LinearLayout?>("chat_list_adapter_item_reply_leading_views")
                ?: return@after

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

        // Configures the guild tag for the message author
        patcher.after<WidgetChatListAdapterItemMessage>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: MessageEntry) ->
            GuildTagView.findIn(itemView)?.configure(entry.message.author.primaryGuild)
        }

        // Configures the guild tag for the reply preview
        patcher.before<WidgetChatListAdapterItemMessage>(
            "configureReplyPreview",
            MessageEntry::class.java,
        ) { (_, entry: MessageEntry) ->
            val referencedAuthor = entry.message.referencedMessage?.e()
            itemView.findViewById<GuildTagView>(replyTagId)
                ?.configure(referencedAuthor?.primaryGuild)
        }
    }

    private fun patchDMsList(patcher: PatcherAPI) {
        // Patches the username in the Direct Messages list
        patcher.after<WidgetChannelsListAdapter.ItemChannelPrivate>(
            Int::class.javaPrimitiveType!!,
            WidgetChannelsListAdapter::class.java,
        ) { _ ->
            val nameView = itemView.findViewById<TextView>("channels_list_item_private_name")
            val parent = nameView.parent as LinearLayout
            parent.gravity = Gravity.CENTER_VERTICAL
            GuildTagView(itemView.context).addTo(parent, 1) {
                setCardBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary))
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    marginStart = 6.dp
                }
            }
        }

        patcher.after<WidgetChannelsListAdapter.ItemChannelPrivate>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChannelListItem::class.java,
        ) { (_, _: Int, item: ChannelListItemPrivate) ->
            ChannelUtils.getDMRecipient(item.channel)?.let { user ->
                GuildTagView.findIn(itemView)?.run {
                    configure(user.primaryGuild)
                }
            }
        }
    }
}
