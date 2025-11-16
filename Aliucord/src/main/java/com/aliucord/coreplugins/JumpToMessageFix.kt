package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.instead
import com.discord.stores.StoreStream
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.chat.list.entries.NewMessagesEntry


internal class JumpToMessageFix : CorePlugin(Manifest("JumpToMessageFix")) {
    init {
        manifest.description = "Fixes message links not jumping to correct message."
    }

    override fun start(context: Context) {
        patcher.instead<WidgetChatListAdapter.ScrollToWithHighlight>("getNewMessageEntryIndex", List::class.java) { param ->
            val list = param.args[0] as List<*>
            var messageId = this.messageId

            if (messageId == 1L) { // reused var for normal scroll ig
                return@instead 0
            }
            if (messageId == 0L) {
                messageId = this.adapter.data.newMessagesMarkerMessageId
                if (messageId <= 0L) {
                    return@instead 0
                }
            }

            if (messageId <= 0L) {
                return@instead -1
            }
            val messageIndex = list
                .indexOfFirst { item -> (item is MessageEntry) && item.message.id == messageId }

            if (messageIndex == -1) {
                return@instead -1
            }
            val newMessageIndex =
                list.subList(0, messageIndex)
                    .indexOfLast { (it is NewMessagesEntry) && it.messageId == messageId }
                    .takeIf { it != -1 }

            // This ensures the app doesn't jump to message before loading its channel.
            val selectedChannel = StoreStream.Companion!!.channelsSelected.id
            val currentChannel = this.adapter.data.channelId
            if (selectedChannel != currentChannel) {
                // Forces ChatListAdapter to retry jumping to message,
                // giving the app more time to load the channel.
                return@instead -1
            }
            return@instead newMessageIndex ?: messageIndex
        }

    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
