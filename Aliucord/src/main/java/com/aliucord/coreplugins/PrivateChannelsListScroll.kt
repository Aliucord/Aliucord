/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.widgets.channels.list.WidgetChannelListModel
import com.discord.widgets.channels.list.WidgetChannelsList

internal class PrivateChannelsListScroll : CorePlugin(Manifest("PrivateChannelsListScroll")) {
    override val isHidden = true
    override val isRequired = true

    override fun load(context: Context) {
        patcher.after<WidgetChannelsList>("configureUI", WidgetChannelListModel::class.java)
        { (_, model: WidgetChannelListModel) ->
            if (!model.isGuildSelected && model.items.size > 1) {
                val manager = WidgetChannelsList.`access$getBinding$p`(this).c.layoutManager!! as LinearLayoutManager
                if (manager.findFirstVisibleItemPosition() != 0) {
                    manager.scrollToPosition(0)
                    patcher.unpatchAll()
                }
            }
        }
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}
}
