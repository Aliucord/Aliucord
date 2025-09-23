/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.rn

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.updater.ManagerBuild
import com.discord.api.user.UserProfile
import de.robv.android.xposed.XposedBridge

internal class RNAPI : CorePlugin(Manifest("RNAPI")) {
    override val isHidden = true
    override val isRequired = true

    override fun load(context: Context) {
        XposedBridge.makeClassInheritable(UserProfile::class.java)

        if (ManagerBuild.hasPatches("1.1.1")) patchGlobalName()
        else logger.warn("Base app outdated, cannot patch display names")

        patchNextCallAdapter()
        patchUserProfile()
        patchDefaultAvatars()
        patchUsername()
        patchStickers()
        patchVoice()
        patchMessageEmbeds()

        if (ManagerBuild.hasInjector("2.1.2")) patchAuditLog()
        else logger.warn("Base app outdated, cannot patch audit log")
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}
}
