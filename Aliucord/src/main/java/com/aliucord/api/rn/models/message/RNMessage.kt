/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api.rn.models.message

import com.aliucord.api.rn.user.RNUser
import com.discord.models.message.Message

class RNMessage(
    message: com.discord.api.message.Message,
    val author: RNUser,
    @JvmField val mentions: List<RNUser>
) : Message(message)
