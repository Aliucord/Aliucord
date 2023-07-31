/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.discord.widgets.channels.list.WidgetChannelListModel
import com.discord.widgets.channels.list.WidgetChannelsList

internal class PrivateChannelsListScroll : Plugin(Manifest("PrivateChannelsListScroll")) {
    override fun load(context: Context?) {
        Patcher.addPatch(WidgetChannelsList::class.java.getDeclaredMethod("configureUI", WidgetChannelListModel::class.java), Hook {
            val model = it.args[0] as WidgetChannelListModel
            if (!model.isGuildSelected) WidgetChannelsList.`access$getBinding$p`(it.thisObject as WidgetChannelsList).c.scrollToPosition(0)
        })
    }

    override fun start(context: Context?) {}
    override fun stop(context: Context?) {}
}
