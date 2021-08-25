/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers

import com.discord.api.channel.Channel
import com.discord.api.channel.ChannelRecipientNick
import com.discord.api.guildhash.GuildHashes
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
    val Channel.bitrate
      get() = c()

    @JvmStatic
    val Channel.defaultAutoArchiveDuration: Int?
      get() = d()

    @JvmStatic
    val Channel.guildHashes: GuildHashes?
      get() = e()

    @JvmStatic
    val Channel.guildId
      get() = f()

    @JvmStatic
    val Channel.icon: String?
      get() = g()

    @JvmStatic
    val Channel.id
      get() = h()

    @JvmStatic
    val Channel.lastMessageId
      get() = i()

    @JvmStatic
    val Channel.memberListId: String
      get() = k()

    @JvmStatic
    val Channel.messageCount: Int?
      get() = l()

    @JvmStatic
    val Channel.name: String
      get() = m()

    @JvmStatic
    val Channel.nicks: List<ChannelRecipientNick>
      get() = n()

    @JvmStatic
    @get:JvmName("isNsfw")
    val Channel.nsfw
      get() = o()

    @JvmStatic
    val Channel.originChannelId
      get() = p()

    @JvmStatic
    val Channel.ownerId
      get() = q()

    @JvmStatic
    val Channel.parentId
      get() = r()

    @JvmStatic
    val Channel.position
      get() = t()

    @JvmStatic
    val Channel.rateLimitPerUser
      get() = u()

    @JvmStatic
    val Channel.recipientIds: List<Long>
      get() = v()

    @JvmStatic
    val Channel.recipients: List<User>
      get() = w()

    @JvmStatic
    val Channel.rtcRegion: String?
      get() = x()

    @JvmStatic
    val Channel.topic: String?
      get() = z()

    @JvmStatic
    val Channel.type
      get() = A()

    @JvmStatic
    val Channel.userLimit
      get() = B()

    @JvmStatic
    fun Channel.isDM() = guildId == 0L

    @JvmStatic
    fun Channel.isGuild() = !isDM()
  }
}
