/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.rn

import android.content.Context
import com.aliucord.entities.Plugin
import com.discord.api.channel.Channel
import com.discord.api.guildmember.GuildMember
import com.discord.api.guildmember.GuildMembersChunk
import com.discord.api.user.*
import com.discord.models.message.Message
import de.robv.android.xposed.XposedBridge

class RNAPI : Plugin(Manifest("RNAPI")) {
    override fun load(context: Context?) {
        XposedBridge.makeClassInheritable(Channel::class.java)
        XposedBridge.makeClassInheritable(GuildMember::class.java)
        XposedBridge.makeClassInheritable(GuildMembersChunk::class.java)
        XposedBridge.makeClassInheritable(com.discord.api.message.Message::class.java)
        XposedBridge.makeClassInheritable(Message::class.java)
        XposedBridge.makeClassInheritable(TypingUser::class.java)
        XposedBridge.makeClassInheritable(User::class.java)
        XposedBridge.makeClassInheritable(UserProfile::class.java)

        patchJsonAdapters()
        patchUser()
        patchUserProfile()
        patchDefaultAvatars()
        patchUsername()
        patchStickers()
        patchVoice()
        fixPersisters()
    }

    override fun start(context: Context?) {}
    override fun stop(context: Context?) {}
}
