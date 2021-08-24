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
  /** Returns the raw (obfuscated) [Guild] Object associated with this wrapper */
  fun raw() = guild

  val afkChannelId: Long?
    get() = getAfkChannelId(guild)

  val afkTimeout: Int
    get() = getAfkTimeout(guild)

  val approximatePresenceCount: Int
    get() = getApproximatePresenceCount(guild)

  val banner: String?
    get() = getBanner(guild)

  val channelUpdates: List<Channel>
    get() = getChannelUpdates(guild)

  val channels: List<Channel>
    get() = getChannels(guild)

  val defaultMessageNotifications: Int?
    get() = getDefaultMessageNotifications(guild)

  val description: String?
    get() = getDescription(guild)

  val emojis: List<GuildEmoji>
    get() = getEmojis(guild)

  val explicitContentFilter: GuildExplicitContentFilter?
    get() = getExplicitContentFilter(guild)

  val features: List<GuildFeature>
    get() = getFeatures(guild)

  val guildHashes: GuildHashes
    get() = getGuildHashes(guild)

  val scheduledEvents: List<GuildScheduledEvent>
    get() = getScheduledEvents(guild)

  val icon: String?
    get() = getIcon(guild)

  val id: Long
    get() = getId(guild)

  val joinedAt: String?
    get() = getJoinedAt(guild)

  val maxVideoChannelUsers: GuildMaxVideoChannelUsers?
    get() = getMaxVideoChannelUsers(guild)

  val approxMemberCount: Int
    get() = getApproxMemberCount(guild)

  val cachedMembers: List<GuildMember>
    get() = getCachedMembers(guild)

  val mfaLevel: Int
    get() = getMfaLevel(guild)

  val name: String
    get() = getName(guild)

  val isNsfw: Boolean
    get() = isNsfw(guild)

  val ownerId: Long
    get() = getOwnerId(guild)

  val preferredLocale: String?
    get() = getPreferredLocale(guild)

  val premiumSubscriptionCount: Int
    get() = getPremiumSubscriptionCount(guild)

  val premiumTier: Int
    get() = getPremiumTier(guild)

  val presences: List<Presence>
    get() = getPresences(guild)

  val publicUpdatesChannelId: Long?
    get() = getPublicUpdatesChannelId(guild)

  val region: String?
    get() = getRegion(guild)

  val roles: List<GuildRole>
    get() = getRoles(guild)

  val rulesChannelId: Long?
    get() = getRulesChannelId(guild)

  val splash: String?
    get() = getSplash(guild)

  val stickers: List<Sticker>
    get() = getStickers(guild)

  val systemChannelFlags: Int
    get() = getSystemChannelFlags(guild)

  val systemChannelId: Long?
    get() = getSystemChannelId(guild)

  val threads: List<Channel>
    get() = getThreads(guild)

  val isUnavailable: Boolean
    get() = isUnavailable(guild)

  val vanityUrlCode: String?
    get() = getVanityUrlCode(guild)

  val verificationLevel: GuildVerificationLevel
    get() = getVerificationLevel(guild)

  companion object {
    @JvmStatic
    fun getAfkChannelId(guild: Guild): Long? = guild.b()

    @JvmStatic
    fun getAfkTimeout(guild: Guild) = guild.c()

    @JvmStatic
    fun getApproximatePresenceCount(guild: Guild) = guild.d()

    @JvmStatic
    fun getBanner(guild: Guild): String? = guild.e()

    @JvmStatic
    fun getChannelUpdates(guild: Guild): List<Channel> = guild.f()

    @JvmStatic
    fun getChannels(guild: Guild): List<Channel> = guild.g()

    @JvmStatic
    fun getDefaultMessageNotifications(guild: Guild): Int? = guild.h()

    @JvmStatic
    fun getDescription(guild: Guild): String? = guild.i()

    @JvmStatic
    fun getEmojis(guild: Guild): List<GuildEmoji> = guild.j()

    @JvmStatic
    fun getExplicitContentFilter(guild: Guild): GuildExplicitContentFilter? = guild.k()

    @JvmStatic
    fun getFeatures(guild: Guild): List<GuildFeature> = guild.l()

    @JvmStatic
    fun getGuildHashes(guild: Guild): GuildHashes = guild.m()

    @JvmStatic
    fun getScheduledEvents(guild: Guild): List<GuildScheduledEvent> = guild.n()

    @JvmStatic
    fun getIcon(guild: Guild): String? = guild.o()

    @JvmStatic
    fun getId(guild: Guild) = guild.p()

    @JvmStatic
    fun getJoinedAt(guild: Guild): String? = guild.q()

    @JvmStatic
    fun getMaxVideoChannelUsers(guild: Guild): GuildMaxVideoChannelUsers? = guild.r()

    @JvmStatic
    fun getApproxMemberCount(guild: Guild) = guild.s()

    @JvmStatic
    fun getCachedMembers(guild: Guild): List<GuildMember> = guild.t()

    @JvmStatic
    fun getMfaLevel(guild: Guild) = guild.u()

    @JvmStatic
    fun getName(guild: Guild): String = guild.v()

    @JvmStatic
    fun isNsfw(guild: Guild) = guild.w()

    @JvmStatic
    fun getOwnerId(guild: Guild) = guild.x()

    @JvmStatic
    fun getPreferredLocale(guild: Guild): String? = guild.y()

    @JvmStatic
    fun getPremiumSubscriptionCount(guild: Guild) = guild.z()

    @JvmStatic
    fun getPremiumTier(guild: Guild) = guild.A()

    @JvmStatic
    fun getPresences(guild: Guild): List<Presence> = guild.B()

    @JvmStatic
    fun getPublicUpdatesChannelId(guild: Guild): Long? = guild.C()

    @JvmStatic
    fun getRegion(guild: Guild): String? = guild.D()

    @JvmStatic
    fun getRoles(guild: Guild): List<GuildRole> = guild.E()

    @JvmStatic
    fun getRulesChannelId(guild: Guild): Long? = guild.F()

    @JvmStatic
    fun getSplash(guild: Guild): String? = guild.G()

    @JvmStatic
    fun getStickers(guild: Guild): List<Sticker> = guild.I()

    @JvmStatic
    fun getSystemChannelFlags(guild: Guild) = guild.J()

    @JvmStatic
    fun getSystemChannelId(guild: Guild): Long? = guild.K()

    @JvmStatic
    fun getThreads(guild: Guild): List<Channel> = guild.L()

    @JvmStatic
    fun isUnavailable(guild: Guild): Boolean = guild.M()

    @JvmStatic
    fun getVanityUrlCode(guild: Guild): String? = guild.N()

    @JvmStatic
    fun getVerificationLevel(guild: Guild): GuildVerificationLevel = guild.O()
  }
}
