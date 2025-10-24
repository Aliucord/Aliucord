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
import com.aliucord.*
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.lazyField
import com.discord.databinding.UserProfileHeaderBadgeBinding
import com.discord.models.guild.Guild
import com.discord.utilities.views.SimpleRecyclerAdapter
import com.discord.widgets.channels.list.WidgetChannelsList
import com.discord.widgets.user.Badge
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.lytefast.flexinput.R

@Suppress("PrivatePropertyName")
internal class SupporterBadges : CorePlugin(MANIFEST) {
    /** Used for the badge in the guild channel list header */
    private val guildBadgeViewId = View.generateViewId()

    /** Badges info that is populated upon plugin start */
    private var badges: BadgesInfo? = null

    // Cached fields
    private val f_badgesAdapter by lazyField<UserProfileHeaderView>("badgesAdapter")
    private val f_recyclerAdapterData by lazyField<SimpleRecyclerAdapter<*, *>>("data")
    private val f_badgeViewHolderBinding by lazyField<UserProfileHeaderView.BadgeViewHolder>("binding")

    @Suppress("UNCHECKED_CAST")
    override fun start(context: Context) {
        Utils.threadPool.execute {
            badges = BadgesAPI(settings).getBadges()
        }

        // Add badges to the RecyclerView data for badges in the user profile header
        patcher.after<UserProfileHeaderView>("updateViewState", UserProfileHeaderViewModel.ViewState.Loaded::class.java)
        { (_, state: UserProfileHeaderViewModel.ViewState.Loaded) ->
            val userBadgesData = badges?.users?.get(state.user.id) ?: return@after
            val roleBadges = userBadgesData.roles?.mapNotNull(::getBadgeForRole) ?: emptyList()
            val customBadges = userBadgesData.custom?.map(::getBadgeForCustom) ?: emptyList()

            val adapter = f_badgesAdapter[this] as SimpleRecyclerAdapter<Badge, UserProfileHeaderView.BadgeViewHolder>
            val data = f_recyclerAdapterData[adapter] as MutableList<Badge>
            data.addAll(roleBadges)
            data.addAll(customBadges)
        }

        // Set image url for badge ImageViews
        patcher.after<UserProfileHeaderView.BadgeViewHolder>("bind", Badge::class.java)
        { (_, badge: Badge) ->
            // Image URL is smuggled through the objectType property
            val url = badge.objectType

            // Check that badge is ours
            if (badge.icon != 0 || url == null) return@after

            val binding = f_badgeViewHolderBinding[this] as UserProfileHeaderBadgeBinding
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
            val badgeData = guild?.id?.let { id -> badges?.guilds?.get(id) }

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

    override fun stop(context: Context) {}

    private companion object {
        val MANIFEST = Manifest(
            name = "SupporterBadges",
            description = "Show badges in the profiles of contributors and donors ♡",
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
