/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api.rn.guildmember

import com.discord.api.guildmember.GuildMembersChunk

class RNGuildMembersChunk(
    @JvmField val members: List<RNGuildMember>
) : GuildMembersChunk()
