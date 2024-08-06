/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api.rn.guildmember

import com.aliucord.api.rn.user.RNUser
import com.discord.api.guildmember.GuildMember
import com.discord.api.presence.Presence
import com.discord.api.utcdatetime.UtcDateTime

class RNGuildMember(
    guildId: Long,
    @JvmField val user: RNUser,
    roles: List<Long>,
    nick: String?,
    premiumSince: String?,
    joinedAt: UtcDateTime?,
    pending: Boolean,
    presence: Presence?,
    userId: Long?,
    avatar: String?,
    bio: String?,
    banner: String?,
    communicationDisabledUntil: UtcDateTime?
) : GuildMember(guildId, user, roles, nick, premiumSince, joinedAt, pending, presence, userId, avatar, bio, banner, communicationDisabledUntil)
