package com.aliucord.coreplugins

import android.content.Context
import androidx.fragment.app.FragmentManager

import com.aliucord.api.ButtonsAPI
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*

import com.discord.models.message.Message
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemBotComponentRow

import de.robv.android.xposed.XposedBridge

import java.util.*
import kotlin.text.startsWith

internal class ButtonsAPI : Plugin(Manifest("ButtonsAPI")) {
    override fun load(context: Context) {
        Patcher.addPatch(WidgetChatListAdapterItemBotComponentRow::class.java.getDeclaredMethod("onButtonComponentClick", Int::class.java, String::class.java), PreHook {
            val _this = it.thisObject as WidgetChatListAdapterItemBotComponentRow
            val customId = it.args[1] as String
            val acId = (-CommandsAPI.ALIUCORD_APP_ID).toString()
            if(customId.startsWith(acId)) { 
                val id = customId.subSequence(CommandsAPI.ALIUCORD_APP_ID.toString().length, customId.length).toString()
                Companion.actions[id]?.invoke(_this.entry.message, _this.adapter.fragmentManager)
                it.result = null
            }
        })
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}

    companion object {
        val actions = HashMap<String, (Message, FragmentManager) -> Unit>()
    }
}