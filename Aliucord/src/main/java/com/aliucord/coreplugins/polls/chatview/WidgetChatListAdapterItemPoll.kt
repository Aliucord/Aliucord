package com.aliucord.coreplugins.polls.chatview

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import com.aliucord.Utils
import com.aliucord.utils.ViewUtils.addTo
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListItem
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.lytefast.flexinput.R

internal class WidgetChatListAdapterItemPoll(adapter: WidgetChatListAdapter)
    : WidgetChatListItem(Utils.getResId("widget_chat_list_adapter_item_minimal", "layout"), adapter) {

    private val pollView: PollChatView
    private val root get() = this.itemView as ConstraintLayout

    init {
        val unusedTextView = root.getViewById(Utils.getResId("chat_list_adapter_item_text", "id"))
        root.removeView(unusedTextView)

        val resources = adapter.context.resources
        val bottom = resources.getDimension(R.d.chat_cell_vertical_spacing_padding).toInt()
        val start = resources.getDimension(R.d.uikit_guideline_chat).toInt()
        val end = resources.getDimension(R.d.chat_cell_horizontal_spacing_total).toInt()
        root.setPadding(start, 0, end, bottom)

        pollView = PollChatView(adapter.context).addTo(root) {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onConfigure(i: Int, data: ChatListEntry) {
        super.onConfigure(i, data)

        if (data !is PollChatEntry) {
            throw IllegalArgumentException("Tried to configure poll view with non-poll data (${data.javaClass.name})")
        }

        val highlightBg = root.getViewById(Utils.getResId("chat_list_adapter_item_highlighted_bg", "id"))
        val gutterBg = root.getViewById(Utils.getResId("chat_list_adapter_item_gutter_bg", "id"))
        configureCellHighlight(data.message, highlightBg, gutterBg)
        pollView.configure(data)
    }
}
