/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api.rn.user

import com.aliucord.api.rn.guildmember.RNGuildMember
import com.discord.api.user.TypingUser

class RNTypingUser(
    @JvmField val member: RNGuildMember?
) : TypingUser()
