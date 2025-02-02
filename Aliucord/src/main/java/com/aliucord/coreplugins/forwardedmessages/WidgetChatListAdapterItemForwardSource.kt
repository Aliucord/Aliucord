@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS") // Just gets rid of the false error inspections

package com.aliucord.coreplugins.forwardedmessages

import android.annotation.SuppressLint
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.aliucord.wrappers.ChannelWrapper.Companion.name
import com.discord.stores.StoreStream
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.drawable.DrawableCompat
import com.discord.utilities.time.ClockFactory
import com.discord.utilities.time.TimeUtils
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListItem
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.lytefast.flexinput.R

class WidgetChatListAdapterItemForwardSource(
    adapter: WidgetChatListAdapter
): WidgetChatListItem(
    /* layoutId = */ Utils.getResId("widget_chat_list_adapter_item_minimal", "layout"), // This layout is the one used by merged messages, it only contains a TextView - making it optimal for what we need,
    /* adapter = */ adapter
) {

    private val content: TextView = itemView.findViewById(Utils.getResId("chat_list_adapter_item_text", "id"))
    private val gutter: View = itemView.findViewById(Utils.getResId("chat_list_adapter_item_gutter_bg", "id")) // Just used to style mentions, we hide it

    private val arrowIcon = DrawableCompat.getDrawable(
        /* context = */ adapter.context,
        /* drawableId = */ R.e.ic_chevron_right_grey_12dp,
        /* color = */ 0
    ).mutate().also { it.setBounds(0, 0, 16.dp, 16.dp) }

    @SuppressLint("SetTextI18n")
    override fun onConfigure(i: Int, data: ChatListEntry?) {
        super.onConfigure(i, data)
        gutter.visibility = View.GONE // This is visible by default for some reason

        if (data is ForwardSourceChatEntry) {
            val channel = StoreStream.getChannels().getChannel(data.reference.a())
            val guild = StoreStream.getGuilds().getGuild(data.reference.b())
            val timestamp = SnowflakeUtils.toTimestamp(data.reference.c())

            (content.parent as ConstraintLayout).setOnClickListener {
                StoreStream.getMessagesLoader().jumpToMessage(channel.id, data.reference.c())
            }

            content.apply {
                setTextColor(ColorCompat.getThemedColor(context, R.b.colorTextMuted))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                gravity = Gravity.CENTER_VERTICAL

                arrowIcon.setTint(ColorCompat.getThemedColor(context, R.b.colorTextMuted)) // This is duplicated

                text = SpannableStringBuilder().apply {
                    val source = if (guild.id != adapter.data.guildId) guild.name else "#${channel.name}"
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
    }

}
