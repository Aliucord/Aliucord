/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.discord.widgets.channels.list.WidgetChannelListModel
import com.discord.widgets.channels.list.WidgetChannelsList
import de.robv.android.xposed.XC_MethodHook

internal class PrivateChannelsListScroll : CorePlugin(Manifest("PrivateChannelsListScroll")) {
    override val isHidden = true
    override val isRequired = true

    private var unhook: XC_MethodHook.Unhook? = null

    override fun load(context: Context?) {
        unhook = Patcher.addPatch(WidgetChannelsList::class.java.getDeclaredMethod("configureUI", WidgetChannelListModel::class.java), Hook {
            val model = it.args[0] as WidgetChannelListModel
            if (!model.isGuildSelected && model.items.size > 1) {
                val manager = WidgetChannelsList.`access$getBinding$p`(it.thisObject as WidgetChannelsList).c.layoutManager!! as LinearLayoutManager
                if (manager.findFirstVisibleItemPosition() != 0) {
                    manager.scrollToPosition(0)
                    unhook?.unhook()
                    unhook = null
                }
            }
        })
    }

    override fun start(context: Context?) {}
    override fun stop(context: Context?) {}
}
