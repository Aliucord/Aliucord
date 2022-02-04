/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import androidx.fragment.app.FragmentActivity

import com.aliucord.api.ButtonsAPI
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*

import com.discord.models.message.Message
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemBotComponentRow

import java.util.*
import kotlin.text.startsWith

internal class ButtonsAPI : Plugin(Manifest("ButtonsAPI")) {
    override fun load(context: Context) {
        patcher.before<WidgetChatListAdapterItemBotComponentRow>("onButtonComponentClick", Int::class.java, String::class.java) {
            val customId = it.args[1] as String
            val acId = (-CommandsAPI.ALIUCORD_APP_ID).toString()
            if (customId.startsWith(acId)) {
                val id = customId.subSequence(CommandsAPI.ALIUCORD_APP_ID.toString().length, customId.length).toString()
                Companion.actions[id]?.invoke(entry.message, adapter.fragmentManager.fragments[0]!!.activity!!)
                it.result = null
            }
        }
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}

    companion object {
        val actions = HashMap<String, (Message, FragmentActivity) -> Unit>()
    }
}
