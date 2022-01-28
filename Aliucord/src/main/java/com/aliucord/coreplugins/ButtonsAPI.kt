package com.aliucord.coreplugins

import android.content.Context

import com.aliucord.Logger
import com.aliucord.api.ButtonsAPI
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*

import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemBotComponentRow

import de.robv.android.xposed.XposedBridge

import java.util.*
import kotlin.text.startsWith

internal class ButtonsAPI : Plugin(Manifest("ButtonsAPI")) {
    override fun load(context: Context) {
        Patcher.addPatch(WidgetChatListAdapterItemBotComponentRow::class.java.getDeclaredMethod("onButtonComponentClick", Int::class.java, String::class.java), InsteadHook {
            val _this = it.thisObject as WidgetChatListAdapterItemBotComponentRow
            val customId = it.args[1] as String
            val entry = _this.entry
            val acId = (-CommandsAPI.ALIUCORD_APP_ID).toString()
            if(!customId.startsWith(acId)) { XposedBridge.invokeOriginalMethod(it.method, it.thisObject, it.args); return@InsteadHook null }
            
            val id = customId.subSequence(CommandsAPI.ALIUCORD_APP_ID.toString().length, customId.length).toString()
        
            Companion.actions[id]?.invoke(ButtonsAPI.ButtonContext(entry.message, _this.itemView.context))
        })
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}

    companion object {
        val actions = HashMap<String, (ButtonsAPI.ButtonContext) -> Unit>()
    }
}