/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.rn

import android.content.Context
import com.aliucord.entities.Plugin
import com.discord.api.user.User
import com.discord.api.user.UserProfile
import de.robv.android.xposed.XposedBridge

class RNAPI : Plugin(Manifest("RNAPI")) {
    override fun load(context: Context?) {
        XposedBridge.makeClassInheritable(User::class.java)
        XposedBridge.makeClassInheritable(UserProfile::class.java)

        patchNextCallAdapter()
        patchUser()
        patchUserProfile()
        patchDefaultAvatars()
    }

    override fun start(context: Context?) {}
    override fun stop(context: Context?) {}
}
