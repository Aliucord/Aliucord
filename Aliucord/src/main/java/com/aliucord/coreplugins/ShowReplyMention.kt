/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.before
import com.discord.models.member.GuildMember
import com.discord.models.user.CoreUser
import com.discord.models.user.User
import com.discord.stores.StoreMessageReplies
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry

internal class ShowReplyMention : CorePlugin(Manifest("ShowReplyMention")) {
    override val isHidden = true
    override val isRequired = true

    init {
        manifest.description = "Fixes showing reply mention"
    }

    override fun start(context: Context) {
        val mConfigureReplyAvatar = WidgetChatListAdapterItemMessage::class.java
            .getDeclaredMethod("configureReplyAvatar", User::class.java, GuildMember::class.java)
            .apply { isAccessible = true }
        val mConfigureReplyName = WidgetChatListAdapterItemMessage::class.java
            .getDeclaredMethod("configureReplyName", String::class.java, Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType)
            .apply { isAccessible = true }
        val mGetAuthorTextColor = WidgetChatListAdapterItemMessage::class.java
            .getDeclaredMethod("getAuthorTextColor", GuildMember::class.java)
            .apply { isAccessible = true }
        val fReplyHolder = WidgetChatListAdapterItemMessage::class.java
            .getDeclaredField("replyHolder")
            .apply { isAccessible = true }
        val fReplyLinkItem = WidgetChatListAdapterItemMessage::class.java
            .getDeclaredField("replyLinkItem")
            .apply { isAccessible = true }

        patcher.before<WidgetChatListAdapterItemMessage>("configureReplyPreview", MessageEntry::class.java) {
            if (fReplyHolder[this] == null || fReplyLinkItem[this] == null) return@before

            val messageEntry = it.args[0] as MessageEntry
            val replyData = messageEntry.replyData
            if (replyData == null || replyData.messageState !is StoreMessageReplies.MessageState.Loaded) return@before

            val refEntry = replyData.messageEntry
            val refAuthor = CoreUser(refEntry.message.author)
            val refAuthorMember = refEntry.author
            mConfigureReplyAvatar(this, refAuthor, refAuthorMember)

            val refAuthorId = refAuthor.id
            mConfigureReplyName(
                this,
                refEntry.nickOrUsernames[refAuthorId] ?: refAuthor.username,
                mGetAuthorTextColor(this, refAuthorMember),
                messageEntry.message.mentions.any { u -> u.id == refAuthorId }
            )
        }

        // configureReplyAuthor was mostly reimplemented in our patch in configureReplyPreview,
        // however it is also used for interactions, so we prevent it from calling only when interactionAuthor is null
        patcher.before<WidgetChatListAdapterItemMessage>(
            "configureReplyAuthor",
            User::class.java,
            GuildMember::class.java,
            MessageEntry::class.java
        ) {
            val messageEntry = it.args[2] as MessageEntry

            if (messageEntry.interactionAuthor == null)
                it.result = null
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
