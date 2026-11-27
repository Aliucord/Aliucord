/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Main
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.ChannelUtils
import com.aliucord.utils.DimenUtils
import com.aliucord.wrappers.users.globalName
import com.discord.stores.StoreStream
import com.discord.utilities.spans.ClickableSpan
import com.discord.widgets.chat.list.adapter.*
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.lytefast.flexinput.R

internal class NewMessages : CorePlugin(Manifest("NewMessages")) {
    override val isHidden = true
    override val isRequired = true

    init {
        manifest.description = "Adds missing message types"
    }

    override fun start(context: Context) {
        addFriendRequestMessage()
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private fun addFriendRequestMessage() = tryPatch("Friend request") {
        patcher.after<WidgetChatListAdapterItemSystemMessage>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: MessageEntry) ->
            if (entry.message.type != FRIEND_REQUEST_MESSAGE_TYPE) return@after

            // add a bit of top padding - Canny
            itemView.layoutParams = (itemView.layoutParams as RecyclerView.LayoutParams).apply { topMargin =
                DimenUtils.dpToPx(10)
            }
            val imageView = WidgetChatListAdapterItemSystemMessage.`access$getBinding$p`(this).f
            val drawable = ContextCompat.getDrawable(itemView.context, R.e.ic_add_friend_plus)?.apply {
                mutate()
                setTint(ContextCompat.getColor(itemView.context, R.c.status_green_600))
            }
            drawable?.let { imageView.setImageDrawable(it) }

            // bigger icon looks better - Canny
            imageView.apply {
                layoutParams.height = DimenUtils.dpToPx(20)
                layoutParams.width = DimenUtils.dpToPx(20)
            }
        }
        patcher.before<`WidgetChatListAdapterItemSystemMessage$getSystemMessage$1`>(
            "invoke",
            Context::class.java
        ) { param ->
            val msg = `$this_getSystemMessage`
            if (msg.type != FRIEND_REQUEST_MESSAGE_TYPE) return@before

            val renderCtx = `$usernameRenderContext` as `WidgetChatListAdapterItemSystemMessage$getSystemMessage$usernameRenderContext$1`
            val color = renderCtx.`$authorRoleColor`
            val targetSpan = ClickableSpan(color, false, null) {
                val roleCtx = `$roleSubscriptionPurchaseContext` as `WidgetChatListAdapterItemSystemMessage$getSystemMessage$roleSubscriptionPurchaseContext$1`
                roleCtx.`this$0`.adapter.eventHandler.onMessageAuthorAvatarClicked(msg, StoreStream.getGuildSelected().selectedGuildId)
            }
            val span = if (`$this_getSystemMessage`.author.id == StoreStream.getUsers().me.id) {
                val channel = StoreStream.getChannelsSelected().selectedChannel
                val recipient = ChannelUtils.getDMRecipient(channel)
                val userName = recipient.globalName ?: recipient.username
                SpannableStringBuilder().apply {
                    append("You accepted ")
                    append(userName, targetSpan, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                    append("'s friend request.")
                }
            } else {
                SpannableStringBuilder().apply {
                    append(`$authorName`, targetSpan, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                    append(" accepted your friend request.")
                }
            }


            param.result = span
        }
    }
    private fun tryPatch(label: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            Main.logger.error("Failed to add message type: \"$label\"", e)
        }
    }
    companion object {
        const val FRIEND_REQUEST_MESSAGE_TYPE = 67
    }
}
