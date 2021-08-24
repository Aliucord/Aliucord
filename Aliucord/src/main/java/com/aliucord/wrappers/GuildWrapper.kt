/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticGuildWrapper")
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

val Guild.afkChannelId: Long?
  get() = b()

val Guild.afkTimeout
  get() = c()

val Guild.approximatePresenceCount
  get() = d()

val Guild.banner: String?
  get() = e()

val Guild.channelUpdates: List<Channel>
  get() = f()

val Guild.channels: List<Channel>
  get() = g()

val Guild.defaultMessageNotifications: Int?
  get() = h()

val Guild.description: String?
  get() = i()

val Guild.emojis: List<GuildEmoji>
  get() = j()

val Guild.explicitContentFilter: GuildExplicitContentFilter?
  get() = k()

val Guild.features: List<GuildFeature>
  get() = l()

val Guild.hashes: GuildHashes
  get() = m()

val Guild.scheduledEvents: List<GuildScheduledEvent>
  get() = n()

val Guild.icon: String?
  get() = o()

val Guild.id
  get() = p()

val Guild.joinedAt: String?
  get() = q()

val Guild.maxVideoChannelUsers: GuildMaxVideoChannelUsers?
  get() = r()

val Guild.approxMemberCount
  get() = s()

val Guild.cachedMembers: List<GuildMember>
  get() = t()

val Guild.mfaLevel
  get() = u()

val Guild.name: String
  get() = v()

@get:JvmName("isNsfw")
val Guild.nsfw
  get() = w()

val Guild.ownerId
  get() = x()

val Guild.preferredLocale: String?
  get() = y()

val Guild.premiumSubscriptionCount
  get() = z()

val Guild.premiumTier
  get() = A()

val Guild.presences: List<Presence>
  get() = B()

val Guild.publicUpdatesChannelId: Long?
  get() = C()

val Guild.region: String?
  get() = D()

val Guild.roles: List<GuildRole>
  get() = E()

val Guild.rulesChannelId: Long?
  get() = F()

val Guild.splash: String?
  get() = G()

val Guild.stickers: List<Sticker>
  get() = I()

val Guild.systemChannelFlags
  get() = J()

val Guild.systemChannelId: Long?
  get() = K()

val Guild.threads: List<Channel>
  get() = L()

@get:JvmName("isUnavailable")
val Guild.unavailable
  get() = M()

val Guild.vanityUrlCode: String?
  get() = N()

val Guild.verificationLevel: GuildVerificationLevel
  get() = O()


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
    @Deprecated(
      "For java: use StaticGuildWrapper.getAfkChannelId\nFor kotlin: use guild.afkChannelId",
      ReplaceWith("guild.afkChannelId"),
      DeprecationLevel.ERROR,
    )
    fun getAfkChannelId(guild: Guild) = guild.afkChannelId

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getAfkTimeout\nFor kotlin: use guild.afkTimeout",
      ReplaceWith("guild.afkTimeout"),
      DeprecationLevel.ERROR,
    )
    fun getAfkTimeout(guild: Guild) = guild.afkTimeout

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getApproximatePresenceCount\nFor kotlin: use guild.approximatePresenceCount",
      ReplaceWith("guild.approximatePresenceCount"),
      DeprecationLevel.ERROR,
    )
    fun getApproximatePresenceCount(guild: Guild) = guild.approximatePresenceCount

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getBanner\nFor kotlin: use guild.banner",
      ReplaceWith("guild.banner"),
      DeprecationLevel.ERROR,
    )
    fun getBanner(guild: Guild) = guild.banner

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getChannelUpdates\nFor kotlin: use guild.channelUpdates",
      ReplaceWith("guild.channelUpdates"),
      DeprecationLevel.ERROR,
    )
    fun getChannelUpdates(guild: Guild) = guild.channelUpdates

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getChannels\nFor kotlin: use guild.channels",
      ReplaceWith("guild.channels"),
      DeprecationLevel.ERROR,
    )
    fun getChannels(guild: Guild) = guild.channels

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getDefaultMessageNotifications\nFor kotlin: use guild.defaultMessageNotifications",
      ReplaceWith("guild.defaultMessageNotifications"),
      DeprecationLevel.ERROR,
    )
    fun getDefaultMessageNotifications(guild: Guild) = guild.defaultMessageNotifications

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getDescription\nFor kotlin: use guild.description",
      ReplaceWith("guild.description"),
      DeprecationLevel.ERROR,
    )
    fun getDescription(guild: Guild) = guild.description

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getEmojis\nFor kotlin: use guild.emojis",
      ReplaceWith("guild.emojis"),
      DeprecationLevel.ERROR,
    )
    fun getEmojis(guild: Guild) = guild.emojis

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getExplicitContentFilter\nFor kotlin: use guild.explicitContentFilter",
      ReplaceWith("guild.explicitContentFilter"),
      DeprecationLevel.ERROR,
    )
    fun getExplicitContentFilter(guild: Guild) = guild.explicitContentFilter

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getFeatures\nFor kotlin: use guild.features",
      ReplaceWith("guild.features"),
      DeprecationLevel.ERROR,
    )
    fun getFeatures(guild: Guild) = guild.features

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getGuildHashes\nFor kotlin: use guild.hashes",
      ReplaceWith("guild.hashes"),
      DeprecationLevel.ERROR,
    )
    fun getGuildHashes(guild: Guild) = guild.hashes

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getScheduledEvents\nFor kotlin: use guild.scheduledEvents",
      ReplaceWith("guild.scheduledEvents"),
      DeprecationLevel.ERROR,
    )
    fun getScheduledEvents(guild: Guild) = guild.scheduledEvents

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getIcon\nFor kotlin: use guild.icon",
      ReplaceWith("guild.icon"),
      DeprecationLevel.ERROR,
    )
    fun getIcon(guild: Guild) = guild.icon

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getId\nFor kotlin: use guild.id",
      ReplaceWith("guild.id"),
      DeprecationLevel.ERROR,
    )
    fun getId(guild: Guild) = guild.id

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getJoinedAt\nFor kotlin: use guild.joinedAt",
      ReplaceWith("guild.joinedAt"),
      DeprecationLevel.ERROR,
    )
    fun getJoinedAt(guild: Guild) = guild.joinedAt

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getMaxVideoChannelUsers\nFor kotlin: use guild.maxVideoChannelUsers",
      ReplaceWith("guild.maxVideoChannelUsers"),
      DeprecationLevel.ERROR,
    )
    fun getMaxVideoChannelUsers(guild: Guild) = guild.maxVideoChannelUsers

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getApproxMemberCount\nFor kotlin: use guild.approxMemberCount",
      ReplaceWith("guild.approxMemberCount"),
      DeprecationLevel.ERROR,
    )
    fun getApproxMemberCount(guild: Guild) = guild.approxMemberCount

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getCachedMembers\nFor kotlin: use guild.cachedMembers",
      ReplaceWith("guild.cachedMembers"),
      DeprecationLevel.ERROR,
    )
    fun getCachedMembers(guild: Guild) = guild.cachedMembers

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getMfaLevel\nFor kotlin: use guild.mfaLevel",
      ReplaceWith("guild.mfaLevel"),
      DeprecationLevel.ERROR,
    )
    fun getMfaLevel(guild: Guild) = guild.mfaLevel

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getName\nFor kotlin: use guild.name",
      ReplaceWith("guild.name"),
      DeprecationLevel.ERROR,
    )
    fun getName(guild: Guild) = guild.name

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.isNsfw\nFor kotlin: use guild.nsfw",
      ReplaceWith("guild.nsfw"),
      DeprecationLevel.ERROR,
    )
    fun isNsfw(guild: Guild) = guild.nsfw

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getOwnerId\nFor kotlin: use guild.ownerId",
      ReplaceWith("guild.ownerId"),
      DeprecationLevel.ERROR,
    )
    fun getOwnerId(guild: Guild) = guild.ownerId

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getPreferredLocale\nFor kotlin: use guild.preferredLocale",
      ReplaceWith("guild.preferredLocale"),
      DeprecationLevel.ERROR,
    )
    fun getPreferredLocale(guild: Guild) = guild.preferredLocale

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getPremiumSubscriptionCount\nFor kotlin: use guild.premiumSubscriptionCount",
      ReplaceWith("guild.premiumSubscriptionCount"),
      DeprecationLevel.ERROR,
    )
    fun getPremiumSubscriptionCount(guild: Guild) = guild.premiumSubscriptionCount

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getPremiumTier\nFor kotlin: use guild.premiumTier",
      ReplaceWith("guild.premiumTier"),
      DeprecationLevel.ERROR,
    )
    fun getPremiumTier(guild: Guild) = guild.premiumTier

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getPresences\nFor kotlin: use guild.presences",
      ReplaceWith("guild.presences"),
      DeprecationLevel.ERROR,
    )
    fun getPresences(guild: Guild) = guild.presences

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getPublicUpdatesChannelId\nFor kotlin: use guild.publicUpdatesChannelId",
      ReplaceWith("guild.publicUpdatesChannelId"),
      DeprecationLevel.ERROR,
    )
    fun getPublicUpdatesChannelId(guild: Guild) = guild.publicUpdatesChannelId

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getRegion\nFor kotlin: use guild.region",
      ReplaceWith("guild.region"),
      DeprecationLevel.ERROR,
    )
    fun getRegion(guild: Guild) = guild.region

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getRoles\nFor kotlin: use guild.roles",
      ReplaceWith("guild.roles"),
      DeprecationLevel.ERROR,
    )
    fun getRoles(guild: Guild) = guild.roles

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getRulesChannelId\nFor kotlin: use guild.rulesChannelId",
      ReplaceWith("guild.rulesChannelId"),
      DeprecationLevel.ERROR,
    )
    fun getRulesChannelId(guild: Guild) = guild.rulesChannelId

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getSplash\nFor kotlin: use guild.splash",
      ReplaceWith("guild.splash"),
      DeprecationLevel.ERROR,
    )
    fun getSplash(guild: Guild) = guild.splash

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getStickers\nFor kotlin: use guild.stickers",
      ReplaceWith("guild.stickers"),
      DeprecationLevel.ERROR,
    )
    fun getStickers(guild: Guild) = guild.stickers

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getSystemChannelFlags\nFor kotlin: use guild.systemChannelFlags",
      ReplaceWith("guild.systemChannelFlags"),
      DeprecationLevel.ERROR,
    )
    fun getSystemChannelFlags(guild: Guild) = guild.systemChannelFlags

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getSystemChannelId\nFor kotlin: use guild.systemChannelId",
      ReplaceWith("guild.systemChannelId"),
      DeprecationLevel.ERROR,
    )
    fun getSystemChannelId(guild: Guild) = guild.systemChannelId

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getThreads\nFor kotlin: use guild.threads",
      ReplaceWith("guild.threads"),
      DeprecationLevel.ERROR,
    )
    fun getThreads(guild: Guild) = guild.threads

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.isUnavailable\nFor kotlin: use guild.unavailable",
      ReplaceWith("guild.unavailable"),
      DeprecationLevel.ERROR,
    )
    fun isUnavailable(guild: Guild) = guild.unavailable

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getVanityUrlCode\nFor kotlin: use guild.vanityUrlCode",
      ReplaceWith("guild.vanityUrlCode"),
      DeprecationLevel.ERROR,
    )
    fun getVanityUrlCode(guild: Guild) = guild.vanityUrlCode

    @JvmStatic
    @Deprecated(
      "For java: use StaticGuildWrapper.getVerificationLevel\nFor kotlin: use guild.verificationLevel",
      ReplaceWith("guild.verificationLevel"),
      DeprecationLevel.ERROR,
    )
    fun getVerificationLevel(guild: Guild) = guild.verificationLevel
  }
}
