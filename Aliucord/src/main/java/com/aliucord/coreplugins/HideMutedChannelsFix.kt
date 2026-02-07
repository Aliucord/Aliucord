package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.after
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.discord.widgets.channels.list.`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$1`
import com.discord.widgets.channels.list.`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$2`

internal class HideMutedChannelsFix : CorePlugin(Manifest("HideMutedChannelsFix")) {
    init {
        manifest.description = "Fixes 'Hide Muted Channels' feature ignoring unread mentions"
    }

    override fun start(context: Context) {
        // Fix hiding muted threads
        patcher.after<`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$1`>("invoke") { param ->
            val builder = this.`this$0`
            val threadId = this.`$textChannel`.id
            val mentionCount = builder.`$mentionCounts$inlined`[threadId] as Int? ?: return@after
            if (mentionCount > 0) {
                builder.`$hiddenChannelsIds$inlined`.remove(threadId)
                param.result = false
            }
        }

        // Fix hiding muted channels
        @Suppress("UNCHECKED_CAST")
        patcher.after<`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$2`>("invoke") { param ->
            val builder = this.`this$0`
            val mentionCountMap = builder.`$mentionCounts$inlined` as Map<Long, Int>
            val hasUnreadMentions = mentionCountMap.values.any { it > 0 }
            if (hasUnreadMentions) {
                builder.`$hiddenChannelsIds$inlined`.remove(this.`$textChannelId`)
                param.result = false
            }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
