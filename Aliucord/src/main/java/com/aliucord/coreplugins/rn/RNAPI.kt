/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.rn

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.discord.api.channel.Channel
import com.discord.api.user.User
import com.discord.api.user.UserProfile
import com.discord.models.message.Message
import de.robv.android.xposed.XposedBridge

internal class RNAPI : CorePlugin(Manifest("RNAPI")) {
    override val isHidden = true
    override val isRequired = true

    override fun load(context: Context?) {
        XposedBridge.makeClassInheritable(Channel::class.java)
        XposedBridge.makeClassInheritable(Message::class.java)
        XposedBridge.makeClassInheritable(User::class.java)
        XposedBridge.makeClassInheritable(UserProfile::class.java)

        patchNextCallAdapter()
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
