/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api.rn.user

import com.discord.api.guildmember.GuildMember
import com.discord.api.premium.PremiumTier
import com.discord.api.user.*
import com.discord.nullserializable.NullSerializable

class RNUser(
    id: Long,
    username: String,
    avatar: NullSerializable<String>?,
    unused: NullSerializable<*>?,
    discriminator: String,
    publicFlags: Int?,
    flags: Int?,
    bot: Boolean?,
    system: Boolean?,
    p9: String?, // all p* are unused in the constructor, discord moment
    p10: String?,
    p11: Boolean?,
    p12: String?,
    p13: NsfwAllowance?,
    p14: Boolean?,
    p15: Phone?,
    p16: String?,
    p17: PremiumTier?,
    p18: Int?,
    p19: GuildMember?,
    p20: NullSerializable<*>?,
    p21: NullSerializable<*>?,
    flags2: Int,
    val globalName: String?
) : User(id, username, avatar, unused, discriminator, publicFlags, flags, bot, system, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, flags2)
