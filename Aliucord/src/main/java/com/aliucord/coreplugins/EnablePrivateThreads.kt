/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.patcher.instead
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemThreadDraftForm
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.ThreadDraftFormEntry

internal class PrivateThreads : Plugin(Manifest("PrivateThreads")) {
    override fun load(context: Context) {
        patcher.instead<ThreadDraftFormEntry>("getCanCreatePrivateThread") { true }

        patcher.after<WidgetChatListAdapterItemThreadDraftForm>("onConfigure", Int::class.javaPrimitiveType!!, ChatListEntry::class.java) {
            this.itemView.findViewById<TextView>(Utils.getResId("private_thread_toggle_badge", "id")).visibility = View.GONE
        }
    }

    override fun start(context: Context?) {}

    override fun stop(context: Context?) {}
}
