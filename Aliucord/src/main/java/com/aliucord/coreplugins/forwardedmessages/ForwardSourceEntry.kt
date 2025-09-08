package com.aliucord.coreplugins.forwardedmessages

import com.discord.api.message.MessageReference
import com.discord.widgets.chat.list.entries.ChatListEntry

internal class ForwardSourceChatEntry(
    val reference: MessageReference,
    private val messageId: Long
): ChatListEntry() {
    override fun getKey(): String {
        // This is just how Discord generates these entry IDs so I copied - Wing (wingio)
        return "$FORWARD_SOURCE_ENTRY_TYPE-$messageId"
    }

    override fun getType(): Int {
        return FORWARD_SOURCE_ENTRY_TYPE
    }

    companion object {
        const val FORWARD_SOURCE_ENTRY_TYPE = 100
    }
}
