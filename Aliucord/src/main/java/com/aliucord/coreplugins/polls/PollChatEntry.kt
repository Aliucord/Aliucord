package com.aliucord.coreplugins.polls

import com.discord.api.message.poll.MessagePoll
import com.discord.models.message.Message
import com.discord.widgets.chat.list.entries.ChatListEntry

internal class PollChatEntry(val poll: MessagePoll, val message: Message) : ChatListEntry() {
    override fun getKey(): String {
        return "$POLL_ENTRY_TYPE-${message.id}"
    }

    override fun getType(): Int {
        return POLL_ENTRY_TYPE
    }

    companion object {
        const val POLL_ENTRY_TYPE = 101 // This is just a random number I picked - Lava (lavadesu)
    }
}
