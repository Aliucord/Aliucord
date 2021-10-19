/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers

import com.discord.api.role.GuildRole

/**
 * Wraps the obfuscated [GuildRole] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class GuildRoleWrapper(private val role: GuildRole) {
  /** Returns the raw (obfuscated) [GuildRole] Object associated with this wrapper */
  fun raw() = role

  val color
    get() = role.color

  val hoist
    get() = role.hoist

  val icon
    get() = role.icon

  val id
    get() = role.id

  val managed
    get() = role.managed

  val mentionable
    get() = role.mentionable

  val name
    get() = role.name

  val permissions
    get() = role.permissions

  val position
    get() = role.position

  val unicodeEmoji
    get() = role.unicodeEmoji

  companion object {
    @JvmStatic
    val GuildRole.color: Int
      get() = b()

    @JvmStatic
    val GuildRole.hoist: Boolean
      get() = c()

    @JvmStatic
    val GuildRole.icon: String
      get() = d()

    @JvmStatic
    val GuildRole.managed: Boolean
      get() = e()

    @JvmStatic
    val GuildRole.mentionable: Boolean
      get() = f()

    @JvmStatic
    val GuildRole.name: String
      get() = g()

    @JvmStatic
    val GuildRole.permissions: Long
      get() = h()

    @JvmStatic
    val GuildRole.position: Int
      get() = i()

    @JvmStatic
    val GuildRole.unicodeEmoji: String
      get() = j()
  }
}
