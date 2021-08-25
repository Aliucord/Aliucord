/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticGuildMemberWrapper")
package com.aliucord.wrappers

import com.discord.api.guildmember.GuildMember
import com.discord.api.user.User
import com.discord.api.utcdatetime.UtcDateTime

val GuildMember.avatar: String?
  get() = b()

val GuildMember.guildId
  get() = c()

val GuildMember.joinedAt: UtcDateTime?
  get() = d()

val GuildMember.nick: String?
  get() = e()

val GuildMember.isPending
  get() = f()

val GuildMember.premiumSince: String?
  get() = g()

val GuildMember.roles: List<Long>
  get() = i()

val GuildMember.user: User
  get() = j()

val GuildMember.userId: Long?
  get() = k()
