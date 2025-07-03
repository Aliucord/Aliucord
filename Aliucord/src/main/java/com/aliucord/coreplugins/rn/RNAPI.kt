/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.rn

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.discord.api.user.UserProfile
import de.robv.android.xposed.XposedBridge

internal class RNAPI : CorePlugin(Manifest("RNAPI")) {
    override val isHidden = true
    override val isRequired = true

    override fun load(context: Context?) {
        XposedBridge.makeClassInheritable(UserProfile::class.java)

        patchNextCallAdapter()
        patchGlobalName()
        patchUserProfile()
        patchDefaultAvatars()
        patchUsername()
        patchStickers()
        patchVoice()
    }

    override fun start(context: Context?) {}
    override fun stop(context: Context?) {}
}
