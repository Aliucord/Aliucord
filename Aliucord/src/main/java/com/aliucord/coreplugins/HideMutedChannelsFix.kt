package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.after
import com.discord.widgets.channels.list.`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1`
import com.discord.widgets.channels.list.`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$2`

@Suppress("MISSING_DEPENDENCY_CLASS")
internal class HideMutedChannelsFix : CorePlugin(Manifest("HideMutedChannelsFix")) {
    init {
        manifest.description = "Fixes 'Hide Muted Channels' feature ignoring unread mentions"
    }

    override fun start(context: Context) {
        patcher.after<`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$2`>("invoke") { param ->
            if (this.`$mentionCount` > 0) {
                val builder = this.`this$0` as `WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1`
                builder.`$hiddenChannelsIds$inlined`.remove(this.`$textChannelId`)
                param.result = false
            }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
