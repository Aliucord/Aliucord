/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.after
import com.aliucord.utils.lazyField
import com.discord.utilities.lazy.memberlist.ChannelMemberList
import com.discord.utilities.lazy.memberlist.MemberListRow
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

@Suppress("PrivatePropertyName")
internal class MembersListFix : CorePlugin(Manifest("MembersListFix")) {
    private val f_memberListGroups by lazyField<ChannelMemberList>("groups")

    override val isHidden = true
    override val isRequired = true

    @Suppress("UNCHECKED_CAST")
    override fun load(context: Context) {
        patcher.after<ChannelMemberList>("setGroups", List::class.java, Function1::class.java) {
            val rows = this.rows
            val groupsMap = f_memberListGroups[this] as Map<String, MemberListRow>
            this.groupIndices.forEach { (idx, id) -> rows[idx] = groupsMap[id] }
        }
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}
}
