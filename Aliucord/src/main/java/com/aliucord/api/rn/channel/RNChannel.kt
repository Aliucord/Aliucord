/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api.rn.channel

import com.aliucord.api.rn.user.RNUser
import com.discord.api.channel.*
import com.discord.api.guildhash.GuildHashes
import com.discord.api.permission.PermissionOverwrite
import com.discord.api.thread.ThreadMember
import com.discord.api.thread.ThreadMetadata

class RNChannel(
    topic: String?,
    type: Int,
    guildId: Long,
    name: String?,
    lastMessageId: Long,
    id: Long,
    ownerId: Long,
    recipientIds: List<Long>?,
    @JvmField val recipients: List<RNUser>?,
    position: Int,
    permissionOverwrites: List<PermissionOverwrite>?,
    userLimit: Int,
    bitrate: Int,
    icon: String?,
    originChannelId: Long,
    applicationId: Long,
    nicks: List<ChannelRecipientNick>?,
    nsfw: Boolean,
    parentId: Long,
    memberListId: String?,
    rateLimitPerUser: Int,
    defaultAutoArchiveDuration: Int?,
    rtcRegion: String?,
    flags: Long?,
    guildHashes: GuildHashes?,
    availableTags: List<ForumTag>?,
    appliedTags: List<Long>?,
    threadMetadata: ThreadMetadata?,
    messageCount: Int?,
    memberCount: Int?,
    memberIdsPreview: List<Long>?,
    threadMember: ThreadMember?
) : Channel(
    topic,
    type,
    guildId,
    name,
    lastMessageId,
    id,
    ownerId,
    recipientIds,
    recipients,
    position,
    permissionOverwrites,
    userLimit,
    bitrate,
    icon,
    originChannelId,
    applicationId,
    nicks,
    nsfw,
    parentId,
    memberListId,
    rateLimitPerUser,
    defaultAutoArchiveDuration,
    rtcRegion,
    flags,
    guildHashes,
    availableTags,
    appliedTags,
    threadMetadata,
    messageCount,
    memberCount,
    memberIdsPreview,
    threadMember
)
