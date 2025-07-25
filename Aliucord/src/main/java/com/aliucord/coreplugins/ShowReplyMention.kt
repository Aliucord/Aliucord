/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.PreHook
import com.discord.models.member.GuildMember
import com.discord.models.user.CoreUser
import com.discord.models.user.User
import com.discord.stores.StoreMessageReplies
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry

internal class ShowReplyMention : CorePlugin(Manifest("ShowReplyMention")) {
    init {
        manifest.description = "Fixes showing reply mention"
    }

    override fun start(context: Context) {
        val user = User::class.java
        val guildMember = GuildMember::class.java
        val messageEntry = MessageEntry::class.java
        val messageItem = WidgetChatListAdapterItemMessage::class.java
        val configureReplyAvatar = messageItem.getDeclaredMethod("configureReplyAvatar", user,
            guildMember).apply { isAccessible = true }
        val configureReplyName = messageItem.getDeclaredMethod("configureReplyName", String::class.java,
            Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType).apply { isAccessible = true }
        val getAuthorTextColor = messageItem.getDeclaredMethod("getAuthorTextColor", guildMember)
            .apply { isAccessible = true }
        val replyHolder = messageItem.getDeclaredField("replyHolder").apply { isAccessible = true }
        val replyLinkItem = messageItem.getDeclaredField("replyLinkItem").apply { isAccessible = true }

        patcher.patch(messageItem.getDeclaredMethod("configureReplyPreview", messageEntry), PreHook {
            if (replyHolder[it.thisObject] == null || replyLinkItem[it.thisObject] == null) return@PreHook

            val messageEntry = it.args[0] as MessageEntry
            val replyData = messageEntry.replyData
            if (replyData == null || replyData.messageState !is StoreMessageReplies.MessageState.Loaded) return@PreHook

            val refEntry = replyData.messageEntry
            val refAuthor = CoreUser(refEntry.message.author)
            val refAuthorMember = refEntry.author
            configureReplyAvatar(it.thisObject, refAuthor, refAuthorMember)

            val refAuthorId = refAuthor.id
            configureReplyName(
                it.thisObject,
                refEntry.nickOrUsernames[refAuthorId] ?: refAuthor.username,
                getAuthorTextColor(it.thisObject, refAuthorMember),
                messageEntry.message.mentions.any { u -> u.id == refAuthorId }
            )
        })

        // configureReplyAuthor was mostly reimplemented in our patch in configureReplyPreview,
        // however it is also used for interactions, so we prevent it from calling only when interactionAuthor is null
        patcher.patch(messageItem.getDeclaredMethod("configureReplyAuthor", user, guildMember, messageEntry), PreHook {
            if ((it.args[2] as MessageEntry).interactionAuthor == null) it.result = null
        })
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
