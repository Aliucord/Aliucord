/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.discord.utilities.lazy.memberlist.ChannelMemberList
import com.discord.utilities.lazy.memberlist.MemberListRow

internal class MembersListFix : CorePlugin(Manifest("MembersListFix")) {
    override val isHidden = true
    override val isRequired = true

    @Suppress("UNCHECKED_CAST")
    override fun load(context: Context?) {
        val groups = ChannelMemberList::class.java.getDeclaredField("groups").apply { isAccessible = true }
        Patcher.addPatch(ChannelMemberList::class.java.getDeclaredMethod("setGroups", List::class.java, Function1::class.java), Hook {
            val list = it.thisObject as ChannelMemberList
            val rows = list.rows
            val groupsMap = groups[list] as Map<String, MemberListRow>
            list.groupIndices.forEach { (idx, id) -> rows[idx] = groupsMap[id] }
        })
    }

    override fun start(context: Context?) {}
    override fun stop(context: Context?) {}
}
