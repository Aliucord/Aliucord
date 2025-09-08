package com.aliucord.coreplugins.polls.chatview

import com.discord.api.message.poll.MessagePoll
import com.discord.models.message.Message
import com.discord.widgets.chat.list.entries.ChatListEntry

internal class PollChatEntry(val poll: MessagePoll, val message: Message) : ChatListEntry() {
    companion object {
        const val POLL_ENTRY_TYPE = 101 // This is just a random number I picked - Lava (lavadesu)
    }

    override fun getKey() = "$POLL_ENTRY_TYPE-${message.id}"
    override fun getType() = POLL_ENTRY_TYPE
}
