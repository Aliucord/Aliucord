/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api.rn.message

import com.aliucord.api.rn.channel.RNChannel
import com.aliucord.api.rn.user.RNUser
import com.discord.api.application.Application
import com.discord.api.botuikit.Component
import com.discord.api.guildmember.GuildMember
import com.discord.api.interaction.Interaction
import com.discord.api.message.Message
import com.discord.api.message.MessageReference
import com.discord.api.message.activity.MessageActivity
import com.discord.api.message.attachment.MessageAttachment
import com.discord.api.message.call.MessageCall
import com.discord.api.message.embed.MessageEmbed
import com.discord.api.message.reaction.MessageReaction
import com.discord.api.message.role_subscription.RoleSubscriptionData
import com.discord.api.sticker.Sticker
import com.discord.api.sticker.StickerPartial
import com.discord.api.utcdatetime.UtcDateTime

class RNMessage(
    id: Long,
    channelId: Long,
    @JvmField val author: RNUser?,
    content: String?,
    timestamp: UtcDateTime?,
    editedTimestamp: UtcDateTime?,
    tts: Boolean?,
    mentionEveryone: Boolean?,
    @JvmField val mentions: List<RNUser>?,
    mentionRoles: List<Long>?,
    attachments: List<MessageAttachment>?,
    embeds: List<MessageEmbed>?,
    reactions: List<MessageReaction>?,
    nonce: String?,
    pinned: Boolean?,
    webhookId: Long?,
    type: Int?,
    activity: MessageActivity?,
    application: Application?,
    applicationId: Long?,
    messageReference: MessageReference?,
    flags: Long?,
    stickers: List<Sticker>?,
    stickerItems: List<StickerPartial>?,
    referencedMessage: RNMessage?,
    interaction: Interaction?,
    @JvmField val thread: RNChannel?,
    components: List<Component>?,
    call: MessageCall?,
    guildId: Long?,
    member: GuildMember?,
    hit: Boolean?,
    roleSubscriptionData: RoleSubscriptionData?,
    constructorFlags: Int,
    constructorFlags2: Int
) : Message(
    id,
    channelId,
    author,
    content,
    timestamp,
    editedTimestamp,
    tts,
    mentionEveryone,
    mentions,
    mentionRoles,
    attachments,
    embeds,
    reactions,
    nonce,
    pinned,
    webhookId,
    type,
    activity,
    application,
    applicationId,
    messageReference,
    flags,
    stickers,
    stickerItems,
    referencedMessage,
    interaction,
    thread,
    components,
    call,
    guildId,
    member,
    hit,
    roleSubscriptionData,
    constructorFlags,
    constructorFlags2
)
