package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.after
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.discord.api.channel.Channel
import com.discord.widgets.channels.list.`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$1`
import com.discord.widgets.channels.list.`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$2`
import com.discord.widgets.guilds.list.`WidgetGuildsListViewModel$createDirectMessageItems$1`

internal class HideMutedChannelsFix : CorePlugin(Manifest("HideMutedChannelsFix")) {
    init {
        manifest.description = "Unhide hidden muted channels when unread mentions exist"
    }

    override fun start(context: Context) {
        // Threads
        patcher.after<`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$1`>("invoke") { param ->
            val builder = this.`this$0`
            val threadId = this.`$textChannel`.id
            val mentionCount = builder.`$mentionCounts$inlined`[threadId] as Int? ?: return@after
            if (mentionCount > 0) {
                builder.`$hiddenChannelsIds$inlined`.remove(threadId)
                param.result = false
            }
        }

        // Channels
        @Suppress("UNCHECKED_CAST")
        patcher.after<`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$2`>("invoke") { param ->
            val builder = this.`this$0`
            val channelId = this.`$textChannelId`
            val mentionCountMap = builder.`$mentionCounts$inlined` as Map<Long, Int>
            val threadParentMap = builder.`$threadParentMap$inlined` as Map<Long, List<Channel>>
            val childThreads = threadParentMap[channelId]

            val hasMentions = this.`$mentionCount` > 0
            val hasChildMentions = childThreads?.any { mentionCountMap.getOrDefault(it.id, 0) > 0 } == true
            if (hasMentions || hasChildMentions) {
                builder.`$hiddenChannelsIds$inlined`.remove(this.`$textChannelId`)
                param.result = false
            }
        }

        // DMs
        patcher.after<`WidgetGuildsListViewModel$createDirectMessageItems$1`>("invoke", Channel::class.java) { (param, channel: Channel) ->
            val result = param.result as Boolean
            param.result = result || channel.id in this.`$mentionCounts`.keys
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
