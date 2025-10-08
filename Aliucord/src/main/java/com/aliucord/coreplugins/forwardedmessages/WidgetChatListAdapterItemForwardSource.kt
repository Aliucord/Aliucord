package com.aliucord.coreplugins.forwardedmessages

import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.utils.ChannelUtils
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.discord.stores.StoreStream
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.drawable.DrawableCompat
import com.discord.utilities.icon.IconUtils
import com.discord.utilities.images.MGImages
import com.discord.utilities.time.ClockFactory
import com.discord.utilities.time.TimeUtils
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListItem
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.facebook.drawee.view.SimpleDraweeView
import com.lytefast.flexinput.R

internal class WidgetChatListAdapterItemForwardSource(
    adapter: WidgetChatListAdapter
): WidgetChatListItem(
    /* layoutId = */ Utils.getResId("widget_chat_list_adapter_item_minimal", "layout"), // This layout is the one used by merged messages, it only contains a TextView - making it optimal for what we need,
    /* adapter = */ adapter
) {
    private val sourceIconId = View.generateViewId()

    private val content: TextView = itemView.findViewById(Utils.getResId("chat_list_adapter_item_text", "id"))
    private val gutter: View = itemView.findViewById(Utils.getResId("chat_list_adapter_item_gutter_bg", "id")) // Just used to style mentions, we hide it
    private val sourceIcon = SimpleDraweeView(itemView.context).apply {
        layoutParams = ConstraintLayout.LayoutParams(ICON_SIZE, ICON_SIZE).apply {
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            marginStart = MARGIN
        }
        id = sourceIconId
        visibility = View.GONE
    }

    private val arrowIcon = DrawableCompat.getDrawable(
        /* context = */ adapter.context,
        /* drawableId = */ R.e.ic_chevron_right_grey_12dp,
        /* color = */ 0
    ).mutate().also { it.setBounds(0, 0, 16.dp, 16.dp) }

    override fun onConfigure(i: Int, data: ChatListEntry) {
        super.onConfigure(i, data)
        if (data !is ForwardSourceChatEntry) return
        gutter.visibility = View.GONE // This is visible by default for some reason

        val channel = StoreStream.getChannels().getChannel(data.reference.a())
        val guildId = data.reference.b()
        val guild = guildId?.let { StoreStream.getGuilds().getGuild(it) ?: return }
        val isDm = guild == null // No guild means the message is from a DM or GDM
        val timestamp = SnowflakeUtils.toTimestamp(data.reference.c())

        val shouldShowIcon = isDm || (guild!!.id != adapter.data.guildId && guild.icon != null)
        val source = when {
            !isDm && guild!!.id != adapter.data.guildId -> guild.name
            else -> ChannelUtils.getDisplayNameOrDefault(channel, adapter.context, true)
        }

        (content.parent as ConstraintLayout).apply {
            (layoutParams as RecyclerView.LayoutParams).topMargin = if (shouldShowIcon) 2.dp else 0 // Increases the space a bit when the icon is shown, less ugly imo - Wing (wingio)
            setOnClickListener {
                StoreStream.getMessagesLoader().jumpToMessage(channel.id, data.reference.c())
            }
        }

        // If I don't do this and add view on constructor, it crashes the app for some reason
        if ((content.parent as ConstraintLayout).findViewById<SimpleDraweeView>(sourceIconId) == null) {
            (content.parent as ConstraintLayout).addView(sourceIcon, 0)
        }

        sourceIcon.apply {
            val iconUrl = when {
                isDm -> IconUtils.getForChannel(channel, 64 /* Icon size */)
                else -> IconUtils.getForGuild(guild)
            }

            visibility = if (shouldShowIcon) View.VISIBLE else View.GONE
            IconUtils.setIcon(this, iconUrl)
            MGImages.setRoundingParams(/* imageView = */ this, /* borderRadius = */ ICON_SIZE * (if (isDm) 0.5f else 0.3f), false, null, null, null)
            setTag(R.f.uikit_icon_url, iconUrl)
        }

        content.apply {
            val color = ColorCompat.getThemedColor(context, R.b.colorTextMuted)

            setTextColor(color)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            (layoutParams as ConstraintLayout.LayoutParams).marginStart = if (shouldShowIcon) MARGIN + ICON_SIZE + ICON_PADDING else MARGIN
            gravity = Gravity.TOP

            arrowIcon.setTint(color)

            text = SpannableStringBuilder().apply {
                append("$source  •  ")
                append("${TimeUtils.toReadableTimeString(context, timestamp, ClockFactory.get())} ")

                // Adds the arrow that indicates we can jump to the source
                // ================================================================
                // We're using this over a compound drawable because the TextView
                // takes up all available space. Wrapping also introduces a problem
                // that would be a lot more annoying to solve.
                val l = length
                append(">")
                val iconSpan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ImageSpan(arrowIcon, ImageSpan.ALIGN_BOTTOM) else ImageSpan(arrowIcon)
                setSpan(iconSpan, l, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private companion object {
        val MARGIN = 58.dp // 38dp avatar + 10dp horizontal space
        val ICON_SIZE = 16.dp
        val ICON_PADDING = 5.dp
    }
}
