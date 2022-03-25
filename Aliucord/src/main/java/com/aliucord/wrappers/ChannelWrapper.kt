/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers

import com.discord.api.channel.*
import com.discord.api.guildhash.GuildHashes
import com.discord.api.permission.PermissionOverwrite
import com.discord.api.thread.ThreadMember
import com.discord.api.thread.ThreadMetadata
import com.discord.api.user.User

/**
 * Wraps the obfuscated [Channel] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class ChannelWrapper(private val channel: Channel) {
    /** Returns the raw (obfuscated) [Channel] Object associated with this wrapper */
    fun raw() = channel

    val applicationId
        get() = channel.applicationId

    val appliedTags
        get() = channel.appliedTags

    val availableTags
        get() = channel.availableTags

    val bitrate
        get() = channel.bitrate

    val defaultAutoArchiveDuration
        get() = channel.defaultAutoArchiveDuration

    val guildHashes
        get() = channel.guildHashes

    val guildId
        get() = channel.guildId

    val icon
        get() = channel.icon

    val id
        get() = channel.id

    val lastMessageId
        get() = channel.lastMessageId

    val member
        get() = channel.member

    val memberListId
        get() = channel.memberListId

    val messageCount
        get() = channel.messageCount

    val name
        get() = channel.name

    val nicks
        get() = channel.nicks

    @get:JvmName("isNsfw")
    val nsfw
        get() = channel.nsfw

    val originChannelId
        get() = channel.originChannelId

    val ownerId
        get() = channel.ownerId

    val parentId
        get() = channel.parentId

    val permissionOverwrites
        get() = channel.permissionOverwrites

    val position
        get() = channel.position

    val rateLimitPerUser
        get() = channel.rateLimitPerUser

    val recipientIds
        get() = channel.recipientIds

    val recipients
        get() = channel.recipients

    val rtcRegion
        get() = channel.rtcRegion

    val threadMetadata
        get() = channel.threadMetadata

    val topic
        get() = channel.topic

    val type
        get() = channel.type

    val userLimit
        get() = channel.userLimit

    fun isDM() = channel.isDM()

    fun isGuild() = channel.isGuild()

    companion object {
        @JvmStatic
        val Channel.applicationId
            get() = b()

        @JvmStatic
        val Channel.appliedTags: List<Long>
            get() = c()

        @JvmStatic
        val Channel.availableTags: List<ForumTag>
            get() = d()

        @JvmStatic
        val Channel.bitrate
            get() = e()

        @JvmStatic
        val Channel.defaultAutoArchiveDuration: Int?
            get() = f()

        @JvmStatic
        val Channel.guildHashes: GuildHashes?
            get() = g()

        @JvmStatic
        val Channel.guildId
            get() = h()

        @JvmStatic
        val Channel.icon: String?
            get() = i()

        @JvmStatic
        val Channel.id
            get() = j()

        @JvmStatic
        val Channel.lastMessageId
            get() = k()

        @JvmStatic
        val Channel.member: ThreadMember?
            get() = l()

        @JvmStatic
        val Channel.memberListId: String
            get() = m()

        @JvmStatic
        val Channel.messageCount: Int?
            get() = n()

        @JvmStatic
        val Channel.name: String
            get() = o()

        @JvmStatic
        val Channel.nicks: List<ChannelRecipientNick>
            get() = p()

        @JvmStatic
        @get:JvmName("isNsfw")
        val Channel.nsfw
            get() = q()

        @JvmStatic
        val Channel.originChannelId
            get() = r()

        @JvmStatic
        val Channel.ownerId
            get() = s()

        @JvmStatic
        val Channel.parentId
            get() = t()

        @JvmStatic
        val Channel.permissionOverwrites: List<PermissionOverwrite>
            get() = u()

        @JvmStatic
        val Channel.position
            get() = v()

        @JvmStatic
        val Channel.rateLimitPerUser
            get() = w()

        @JvmStatic
        val Channel.recipientIds: List<Long>
            get() = x()

        @JvmStatic
        val Channel.recipients: List<User>?
            get() = y()

        @JvmStatic
        val Channel.rtcRegion: String?
            get() = z()

        @JvmStatic
        val Channel.threadMetadata: ThreadMetadata?
            get() = A()

        @JvmStatic
        val Channel.topic: String?
            get() = B()

        @JvmStatic
        val Channel.type
            get() = C()

        @JvmStatic
        val Channel.userLimit
            get() = D()

        @JvmStatic
        fun Channel.isDM() = guildId == 0L

        @JvmStatic
        fun Channel.isGuild() = !isDM()
    }
}
