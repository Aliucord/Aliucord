package com.aliucord.coreplugins.decorations.avatar

import android.content.Context
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliucord.api.PatcherAPI
import com.aliucord.coreplugins.decorations.Decorator
import com.aliucord.patcher.*
import com.aliucord.utils.ChannelUtils
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.findViewById
import com.aliucord.utils.ViewUtils.leftPadding
import com.aliucord.utils.ViewUtils.padding
import com.aliucord.utils.ViewUtils.setPadding
import com.aliucord.utils.ViewUtils.topPadding
import com.aliucord.utils.accessField
import com.aliucord.wrappers.users.avatarDecorationData
import com.discord.api.sticker.BaseSticker
import com.discord.api.user.AvatarDecoration
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.databinding.WidgetChannelsListItemChannelPrivateBinding
import com.discord.stores.StoreStream
import com.discord.utilities.icon.IconUtils
import com.discord.utilities.stickers.StickerUtils
import com.discord.views.sticker.StickerView
import com.discord.views.user.UserAvatarPresenceView
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItemPrivate
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.facebook.drawee.view.SimpleDraweeView
import com.lytefast.flexinput.R

private val ChannelMembersListViewHolderMember.binding by accessField<WidgetChannelMembersListItemUserBinding>()
private val WidgetChatListAdapterItemMessage.itemAvatar by accessField<ImageView?>()
private val WidgetChatListAdapterItemMessage.replyHolder by accessField<FrameLayout?>()
private val WidgetChannelsListAdapter.ItemChannelPrivate.binding by accessField<WidgetChannelsListItemChannelPrivateBinding>()

private const val memberListSpacing = 3
private const val messageAuthorSpacing = 4
private const val profileHeaderSpacing = 8

private val decoId = View.generateViewId()

internal class AvatarDecorator() : Decorator() {
    private fun createDecoView(context: Context, animated: Boolean): View {
        return if (animated) {
            StickerView(context, null).apply {
                id = decoId
                // Removes built-in padding
                j.b.setPadding(0)
            }
        } else {
            SimpleDraweeView(context).apply {
                id = decoId
            }
        }
    }

    private fun findAndConfigure(parent: View, data: AvatarDecoration?) {
        parent.findViewById<View>(decoId)?.run {
            if (data != null) {
                visibility = View.VISIBLE
                if (this is StickerView) {
                    d(AvatarSticker(data), null)
                } else if (this is SimpleDraweeView) {
                    IconUtils.setIcon(
                        this,
                        "https://cdn.discordapp.com/avatar-decoration-presets/${data.asset}.png?size=256&passthrough=true",
                    )
                }
            } else {
                visibility = View.INVISIBLE
            }
        }
    }

    override fun patch(patcher: PatcherAPI) {
        patcher.before<StickerUtils>(
            "getCDNAssetUrl",
            BaseSticker::class.java,
            Int::class.javaObjectType,
            Boolean::class.javaPrimitiveType!!,
        ) { (param, sticker: BaseSticker) ->
            if (sticker is AvatarSticker) {
                param.result = "https://cdn.discordapp.com/avatar-decoration-presets/${sticker.data.asset}.png?size=256&passthrough=true"
            }
        }

        patcher.after<UserAvatarPresenceView>(
            "onMeasure",
            Int::class.javaPrimitiveType!!,
            Int::class.javaPrimitiveType!!,
        ) { _ ->
            j.c.layoutParams = j.c.layoutParams.apply {
                width = width - paddingLeft - paddingRight
                height = height - paddingLeft - paddingRight
            }
        }
    }

    override fun onDMsListInit(
        holder: WidgetChannelsListAdapter.ItemChannelPrivate,
        adapter: WidgetChannelsListAdapter
    ) {
        val layout = holder.binding.a
        val avatarView = holder.binding.c
        val size = layout.resources.getDimensionPixelSize(R.d.avatar_size_standard) + (memberListSpacing * 2).dp

        // Removes padding from layout to make space for deco
        layout.padding -= memberListSpacing.dp

        // Re-add left padding to avatar view, that was removed above
        avatarView.layoutParams = (avatarView.layoutParams as RelativeLayout.LayoutParams).apply {
            marginStart += memberListSpacing.dp
        }

        createDecoView(layout.context, false).addTo(layout, layout.indexOfChild(avatarView) + 1) {
            layoutParams = RelativeLayout.LayoutParams(0, 0).apply {
                height = size
                width = size
                marginStart = memberListSpacing.dp
            }
        }
    }

    override fun onDMsListConfigure(
        holder: WidgetChannelsListAdapter.ItemChannelPrivate,
        item: ChannelListItemPrivate
    ) {
        val data = ChannelUtils.getDMRecipient(item.channel)?.avatarDecorationData
        findAndConfigure(holder.binding.a, data)
    }

    override fun onMembersListInit(
        holder: ChannelMembersListViewHolderMember,
        binding: WidgetChannelMembersListItemUserBinding
    ) {
        val layout = binding.a
        val avatarView = binding.b
        val size = layout.resources.getDimensionPixelSize(R.d.avatar_size_standard) + (memberListSpacing * 2).dp

        // Remove the container left padding, and add it in avatar instead
        // This allows space for the deco
        binding.a.leftPadding = 0
        avatarView.layoutParams = (avatarView.layoutParams as ConstraintLayout.LayoutParams).apply {
            marginStart = 16.dp
        }

        // The index is added to allow the status icons higher priority
        createDecoView(layout.context, false).addTo(layout, layout.indexOfChild(avatarView) + 1) {
            layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                height = size
                width = size
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                marginStart = 16.dp - memberListSpacing.dp
                verticalBias = 0.5f
            }
        }
    }

    override fun onMembersListConfigure(
        holder: ChannelMembersListViewHolderMember,
        item: ChannelMembersListAdapter.Item.Member,
        adapter: ChannelMembersListAdapter
    ) {
        val layout = holder.binding.a
        val user = StoreStream.getUsers().users[item.userId]
        val member = StoreStream.getGuilds().getMember(item.guildId ?: -1, item.userId)
        val data = member?.avatarDecorationData ?: user?.avatarDecorationData
        findAndConfigure(layout, data)
    }

    override fun onMessageInit(
        holder: WidgetChatListAdapterItemMessage,
        adapter: WidgetChatListAdapter
    ) {
        val view = holder.itemView as ViewGroup
        val size = view.resources.getDimensionPixelSize(R.d.avatar_size_chat)

        val itemAvatar = holder.itemAvatar ?: return
        val replyHolder = holder.replyHolder ?: return
        val itemHeader = view.findViewById<ConstraintLayout?>("chat_list_adapter_item_text_header")
            ?: return

        // Reduce the container padding to get extra space for decorations
        view.topPadding -= messageAuthorSpacing.dp

        // Expand the avatar height and width by 4.dp, then pad it down and right by 4.dp
        // Essentially a topMargin and leftMargin, but by doing it this way, the avatar's "top"
        // stays the same (since padding), and allows us to align our decorations from there
        itemAvatar.run {
            topPadding += messageAuthorSpacing.dp
            leftPadding += messageAuthorSpacing.dp
            layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                height = size + messageAuthorSpacing.dp
                width = size + messageAuthorSpacing.dp
                marginStart -= messageAuthorSpacing.dp
            }
        }

        // Lower the header by 4.dp from the padding we removed from the container, but also add a bit more to
        // compensate for the extra bottom margin from looking "off"
        itemHeader.topPadding += (messageAuthorSpacing + 2).dp

        // Removes the reply's bottom margin, since the spacing is already applied by the header
        replyHolder.layoutParams = (replyHolder.layoutParams as ConstraintLayout.LayoutParams).apply {
            bottomMargin = 0.dp
        }

        // The decorations view. Align it to the top of the avatar view (4.dp above the image)
        // and add appropriate margins around for a nice, +4.dp overlay all around
        createDecoView(view.context, false).addTo(view) {
            id = decoId
            layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                height = size + (messageAuthorSpacing * 2).dp
                width = size + (messageAuthorSpacing * 2).dp
                startToStart = itemAvatar.id
                topToTop = itemAvatar.id
            }
        }
    }

    override fun onMessageConfigure(
        holder: WidgetChatListAdapterItemMessage,
        entry: MessageEntry
    ) {
        val data = entry.author?.avatarDecorationData ?: entry.message.author.avatarDecorationData
        findAndConfigure(holder.itemView, data)
    }

    override fun onProfileHeaderInit(view: UserProfileHeaderView) {
        val spacedSize = view.resources.getDimensionPixelSize(R.d.avatar_wrap_size_xxlarge) + profileHeaderSpacing.dp

        val binding = UserProfileHeaderView.`access$getBinding$p`(view)
        val avatarView = binding.f
        // Enlarge the avatarView
        avatarView.layoutParams = avatarView.layoutParams.apply {
            width = spacedSize
            height = spacedSize
        }
        // Increase the cutout spacing (that will then be shrunk in earlier onMeasure patch)
        avatarView.l = 4.dp + (profileHeaderSpacing.dp / 2)
        // Set appropriate padding on the cutout view for earlier onMeasure patch
        avatarView.j.c.setPadding(profileHeaderSpacing.dp / 2)

        // Add the overlay in the avatar container: between the avatar and the status cutout
        val avatarcontainer = avatarView.findViewById<FrameLayout>("avatar_container")
        createDecoView(view.context, true).addTo(avatarcontainer) {
            layoutParams = FrameLayout.LayoutParams(0, 0).apply {
                width = spacedSize
                height = spacedSize
                gravity = Gravity.CENTER
            }
        }

        // Remove the name's top margin as a result of the avatarView's size increasing
        val nameLayout = view.findViewById<LinearLayout>("user_profile_header_name_wrap")
        nameLayout.layoutParams = (nameLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
            topMargin = (8 - profileHeaderSpacing).dp
        }
    }

    override fun onProfileHeaderConfigure(
        view: UserProfileHeaderView,
        state: UserProfileHeaderViewModel.ViewState.Loaded
    ) {
        val data = state.guildMember?.avatarDecorationData ?: state.user.avatarDecorationData
        findAndConfigure(view, data)
    }
}
