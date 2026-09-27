/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.badges

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.aliucord.Utils
import com.aliucord.api.rn.user.RNUserProfile
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.accessField
import com.discord.api.user.UserProfile
import com.discord.databinding.UserProfileHeaderBadgeBinding
import com.discord.models.guild.Guild
import com.discord.models.user.User
import com.discord.widgets.channels.list.WidgetChannelsList
import com.discord.widgets.user.Badge
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.lytefast.flexinput.R

internal class Badges : CorePlugin(MANIFEST) {
    /** Used for the badge in the guild channel list header */
    private val guildBadgeViewId = View.generateViewId()

    /** Badges info that is populated upon plugin start */
    private var aliucordBadges: BadgesInfo? = null

    // Cached fields
    private val UserProfileHeaderView.BadgeViewHolder.binding
        by accessField<UserProfileHeaderBadgeBinding>()

    @Suppress("UNCHECKED_CAST")
    override fun start(context: Context) {
        Utils.threadPool.execute {
            aliucordBadges = BadgesAPI(settings).getBadges()
        }

        // Replace default badge getter
        patcher.instead<Badge.Companion>(
            "getBadgesForUser",
            User::class.java,
            UserProfile::class.java,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Context::class.java,
        ) { (_, user: User, profileArg: UserProfile) ->
            val profile = profileArg as? RNUserProfile ?: return@instead listOf<Badge>()
            @OptIn(ExperimentalStdlibApi::class)
            buildList {
                profile.badges?.map { badgeData ->
                    val iconUrl = badgeData.simpleIconUrl
                        ?: "https://cdn.discordapp.com/badge-icons/${badgeData.icon}.png"

                    Badge(
                        /* iconRes = */ 0,
                        /* text = */ null,
                        /* tooltip = */ badgeData.description,
                        /* showPremiumUpSell = */ false,
                        /* objectType = */ iconUrl,
                    )
                }?.let { addAll(it.reversed()) }

                aliucordBadges?.users?.get(user.id)?.let { data ->
                    data.roles?.mapNotNull(::getBadgeForRole)?.let { addAll(it) }
                    data.custom?.map(::getBadgeForCustom)?.let { addAll(it) }
                }
            }
        }

        // Set image url for badge ImageViews
        patcher.after<UserProfileHeaderView.BadgeViewHolder>("bind", Badge::class.java)
        { (_, badge: Badge) ->
            // Image URL is smuggled through the objectType property
            val url = badge.objectType

            // Check that badge is ours
            if (badge.icon != 0 || url == null) return@after

            val imageView = binding.b
            imageView.setCacheableImage(url)
        }

        // Add blank ImageView to the channels list
        patcher.after<WidgetChannelsList>("onViewBound", View::class.java) {
            val binding = WidgetChannelsList.`access$getBinding$p`(this)
            val toolbar = binding.g.parent as ViewGroup
            val imageView = ImageView(toolbar.context).apply {
                id = guildBadgeViewId
                setPadding(0, 0, 4.dp, 0)
            }

            if (toolbar.getChildAt(0).id != guildBadgeViewId)
                toolbar.addView(imageView, 0)
        }

        // Configure the channels list's newly added ImageView to show target guild badge
        patcher.after<WidgetChannelsList>("configureHeaderIcons", Guild::class.java, Boolean::class.javaPrimitiveType!!)
        { (_, guild: Guild?) ->
            val badgeData = guild?.id?.let { id -> aliucordBadges?.guilds?.get(id) }

            if (this.view == null) return@after
            val binding = WidgetChannelsList.`access$getBinding$p`(this)
            val toolbar = binding.g.parent as ViewGroup

            toolbar.findViewById<ImageView>(guildBadgeViewId)?.apply {
                if (badgeData == null) visibility = View.GONE
                else {
                    visibility = View.VISIBLE
                    setCacheableImage(badgeData.url)
                    setOnClickListener { Utils.showToast(badgeData.text) }
                }
            }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private companion object {
        val MANIFEST = Manifest(
            name = "Badges",
            description = "Show new discord badges, plus special ones in the profiles of contributors and donors ♡",
        )

        val DEV_BADGE = Badge(R.e.ic_staff_badge_blurple_24dp, null, "Aliucord Developer", false, null)
        val DONOR_BADGE = Badge(0, null, "Aliucord Donor", false, "https://cdn.discordapp.com/emojis/859801776232202280.webp")
        val CONTRIB_BADGE = Badge(0, null, "Aliucord Contributor", false, "https://cdn.discordapp.com/emojis/886587553187246120.webp")

        fun getBadgeForRole(role: String): Badge? = when (role) {
            "dev" -> DEV_BADGE
            "donor" -> DONOR_BADGE
            "contributor" -> CONTRIB_BADGE
            else -> null
        }

        fun getBadgeForCustom(data: BadgeData): Badge =
            Badge(0, null, data.text, false, data.url)
    }
}
