/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers

import com.discord.api.channel.Channel
import com.discord.api.emoji.GuildEmoji
import com.discord.api.guild.*
import com.discord.api.guildhash.GuildHashes
import com.discord.api.guildmember.GuildMember
import com.discord.api.guildscheduledevent.GuildScheduledEvent
import com.discord.api.presence.Presence
import com.discord.api.role.GuildRole
import com.discord.api.sticker.Sticker

/**
 * Wraps the obfuscated [Guild] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class GuildWrapper(private val guild: Guild) {
  val Guild.test
    get() = "aaa"

  /** Returns the raw (obfuscated) [Guild] Object associated with this wrapper */
  fun raw() = guild

  val afkChannelId
    get() = guild.afkChannelId

  val afkTimeout
    get() = guild.afkTimeout

  val approximatePresenceCount
    get() = guild.approximatePresenceCount

  val banner
    get() = guild.banner

  val channelUpdates
    get() = guild.channelUpdates

  val channels
    get() = guild.channels

  val defaultMessageNotifications
    get() = guild.defaultMessageNotifications

  val description
    get() = guild.description

  val emojis
    get() = guild.emojis

  val explicitContentFilter
    get() = guild.explicitContentFilter

  val features
    get() = guild.features

  val guildHashes
    get() = guild.hashes

  val scheduledEvents
    get() = guild.scheduledEvents

  val icon
    get() = guild.icon

  val id
    get() = guild.id

  val joinedAt
    get() = guild.joinedAt

  val maxVideoChannelUsers: GuildMaxVideoChannelUsers?
    get() = guild.maxVideoChannelUsers

  val approxMemberCount
    get() = guild.approxMemberCount

  val cachedMembers
    get() = guild.cachedMembers

  val mfaLevel
    get() = guild.mfaLevel

  val name
    get() = guild.name

  @get:JvmName("isNsfw")
  val nsfw
    get() = guild.nsfw

  val ownerId
    get() = guild.ownerId

  val preferredLocale
    get() = guild.preferredLocale

  val premiumSubscriptionCount
    get() = guild.premiumSubscriptionCount

  val premiumTier
    get() = guild.premiumTier

  val presences
    get() = guild.presences

  val publicUpdatesChannelId
    get() = guild.publicUpdatesChannelId

  val region
    get() = guild.region

  val roles
    get() = guild.roles

  val rulesChannelId
    get() = guild.rulesChannelId

  val splash
    get() = guild.splash

  val stickers
    get() = guild.stickers

  val systemChannelFlags
    get() = guild.systemChannelFlags

  val systemChannelId
    get() = guild.systemChannelId

  val threads
    get() = guild.threads

  @get:JvmName("isUnavailable")
  val unavailable
    get() = guild.unavailable

  val vanityUrlCode
    get() = guild.vanityUrlCode

  val verificationLevel
    get() = guild.verificationLevel

  companion object {
    @JvmStatic
    val Guild.afkChannelId: Long?
      get() = b()

    @JvmStatic
    val Guild.afkTimeout
      get() = c()

    @JvmStatic
    val Guild.approximatePresenceCount
      get() = d()

    @JvmStatic
    val Guild.banner: String?
      get() = e()

    @JvmStatic
    val Guild.channelUpdates: List<Channel>
      get() = f()

    @JvmStatic
    val Guild.channels: List<Channel>
      get() = g()

    @JvmStatic
    val Guild.defaultMessageNotifications: Int?
      get() = h()

    @JvmStatic
    val Guild.description: String?
      get() = i()

    @JvmStatic
    val Guild.emojis: List<GuildEmoji>
      get() = k()

    @JvmStatic
    val Guild.explicitContentFilter: GuildExplicitContentFilter?
      get() = l()

    @JvmStatic
    val Guild.features: List<GuildFeature>
      get() = m()

    @JvmStatic
    val Guild.hashes: GuildHashes
      get() = n()

    @JvmStatic
    val Guild.scheduledEvents: List<GuildScheduledEvent>
      get() = o()

    @JvmStatic
    val Guild.icon: String?
      get() = p()

    @JvmStatic
    val Guild.id
      get() = q()

    @JvmStatic
    val Guild.joinedAt: String?
      get() = r()

    @JvmStatic
    val Guild.maxVideoChannelUsers: GuildMaxVideoChannelUsers?
      get() = s()

    @JvmStatic
    val Guild.approxMemberCount
      get() = t()

    @JvmStatic
    val Guild.cachedMembers: List<GuildMember>
      get() = u()

    @JvmStatic
    val Guild.mfaLevel
      get() = v()

    @JvmStatic
    val Guild.name: String
      get() = w()

    @JvmStatic
    @get:JvmName("isNsfw")
    val Guild.nsfw
      get() = x()

    @JvmStatic
    val Guild.ownerId
      get() = y()

    @JvmStatic
    val Guild.preferredLocale: String?
      get() = z()

    @JvmStatic
    val Guild.premiumSubscriptionCount
      get() = A()

    @JvmStatic
    val Guild.premiumTier
      get() = B()

    @JvmStatic
    val Guild.presences: List<Presence>
      get() = C()

    @JvmStatic
    val Guild.publicUpdatesChannelId: Long?
      get() = D()

    @JvmStatic
    val Guild.region: String?
      get() = E()

    @JvmStatic
    val Guild.roles: List<GuildRole>
      get() = F()

    @JvmStatic
    val Guild.rulesChannelId: Long?
      get() = G()

    @JvmStatic
    val Guild.splash: String?
      get() = H()

    @JvmStatic
    val Guild.stickers: List<Sticker>
      get() = J()

    @JvmStatic
    val Guild.systemChannelFlags
      get() = K()

    @JvmStatic
    val Guild.systemChannelId: Long?
      get() = L()

    @JvmStatic
    val Guild.threads: List<Channel>
      get() = M()

    @JvmStatic
    @get:JvmName("isUnavailable")
    val Guild.unavailable
      get() = N()

    @JvmStatic
    val Guild.vanityUrlCode: String?
      get() = O()

    @JvmStatic
    val Guild.verificationLevel: GuildVerificationLevel
      get() = P()
  }
}
