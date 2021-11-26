/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers

import com.discord.api.guildmember.GuildMember
import com.discord.api.presence.Presence
import com.discord.api.user.User
import com.discord.api.utcdatetime.UtcDateTime

/**
 * Wraps the obfuscated [GuildMember] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class GuildMemberWrapper(private val guildMember: GuildMember) {
  /** Returns the raw (obfuscated) [GuildMember] Object associated with this wrapper */
  fun raw() = guildMember

  val avatar
    get() = guildMember.avatar

  val banner
    get() = guildMember.banner

  val bio
    get() = guildMember.bio

  val guildId
    get() = guildMember.guildId

  val joinedAt
    get() = guildMember.joinedAt

  val nick
    get() = guildMember.nick

  val isPending
    get() = guildMember.isPending

  val premiumSince
    get() = guildMember.premiumSince

  val presence
    get() = guildMember.presence

  val roles
    get() = guildMember.roles

  val user
    get() = guildMember.user

  val userId
    get() = guildMember.userId

  companion object {
    @JvmStatic
    val GuildMember.avatar: String?
      get() = b()

    @JvmStatic
    val GuildMember.banner: String?
      get() = c()

    @JvmStatic
    val GuildMember.bio: String?
      get() = d()

    @JvmStatic
    val GuildMember.communicationDisabledUntil: UtcDateTime?
      get() = e()

    @JvmStatic
    val GuildMember.guildId
      get() = f()

    @JvmStatic
    val GuildMember.joinedAt: UtcDateTime?
      get() = g()

    @JvmStatic
    val GuildMember.nick: String?
      get() = h()

    @JvmStatic
    val GuildMember.isPending
      get() = i()

    @JvmStatic
    val GuildMember.premiumSince: String?
      get() = j()

    @JvmStatic
    val GuildMember.presence: Presence?
      get() = k()

    @JvmStatic
    val GuildMember.roles: List<Long>
      get() = l()

    @JvmStatic
    val GuildMember.user: User?
      get() = m()

    @JvmStatic
    val GuildMember.userId: Long?
      get() = n()
  }
}
