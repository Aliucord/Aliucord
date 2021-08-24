/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticChannelWrapper")
package com.aliucord.wrappers

import com.discord.api.channel.Channel
import com.discord.api.channel.ChannelRecipientNick
import com.discord.api.guildhash.GuildHashes
import com.discord.api.user.User

val Channel.applicationId
  get() = b()

val Channel.bitrate
  get() = c()

val Channel.defaultAutoArchiveDuration: Int?
  get() = d()

val Channel.guildHashes: GuildHashes?
  get() = e()

val Channel.guildId
  get() = f()

val Channel.icon: String?
  get() = g()

val Channel.id
  get() = h()

val Channel.lastMessageId
  get() = i()

val Channel.memberListId: String
  get() = k()

val Channel.messageCount: Int?
  get() = l()

val Channel.name: String
  get() = m()

val Channel.nicks: List<ChannelRecipientNick>
  get() = n()

@get:JvmName("isNsfw")
val Channel.nsfw
  get() = o()

val Channel.originChannelId
  get() = p()

val Channel.ownerId
  get() = q()

val Channel.parentId
  get() = r()

val Channel.position
  get() = t()

val Channel.rateLimitPerUser
  get() = u()

val Channel.recipientIds: List<Long>
  get() = v()

val Channel.recipients: List<User>
  get() = w()

val Channel.rtcRegion: String?
  get() = x()

val Channel.topic: String?
  get() = z()

val Channel.type
  get() = A()

val Channel.userLimit
  get() = B()

fun Channel.isDM() = guildId == 0L

fun Channel.isGuild() = !isDM()
