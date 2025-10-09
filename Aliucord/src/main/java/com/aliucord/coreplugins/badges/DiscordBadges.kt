package com.aliucord.coreplugins.badges

import android.content.Context
import com.aliucord.api.rn.user.RNUserProfile
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.lazyField
import com.discord.databinding.UserProfileHeaderBadgeBinding
import com.discord.utilities.views.SimpleRecyclerAdapter
import com.discord.widgets.user.Badge
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel

internal class DiscordBadges : CorePlugin(MANIFEST) {
    // Cached fields
    private val f_badgesAdapter by lazyField<UserProfileHeaderView>("badgesAdapter")
    private val f_recyclerAdapterData by lazyField<SimpleRecyclerAdapter<*, *>>("data")
    private val f_badgeViewHolderBinding by lazyField<UserProfileHeaderView.BadgeViewHolder>("binding")

    private val excludedBadgeIds = arrayOf(
        "guild_booster",
        "hypesquad",
        "premium",
        "bug_hunter",
        "verified_developer",
        "staff",
        "early_supporter",
        "partner"
    )

    @Suppress("UNCHECKED_CAST")
    override fun start(context: Context) {
        // Add profile badges to the RecyclerView
        patcher.after<UserProfileHeaderView>(
            "updateViewState", 
            UserProfileHeaderViewModel.ViewState.Loaded::class.java,
        ) { (_, state: UserProfileHeaderViewModel.ViewState.Loaded) ->
            val profile = state.userProfile as? RNUserProfile ?: return@after
            val badges = profile.badges ?: return@after

            // Exclude badges that are already in aliucord
            val discordBadges = badges
                .filterNot { badgeData -> 
                    excludedBadgeIds.any { excludedId -> badgeData.id.contains(excludedId) }
                }
                .map { badgeData ->
                    val iconUrl = "https://cdn.discordapp.com/badge-icons/${badgeData.icon}.png"
                    Badge(0, null, badgeData.description, false, iconUrl)
                }

            val adapter = f_badgesAdapter[this] as SimpleRecyclerAdapter<Badge, UserProfileHeaderView.BadgeViewHolder>
            val data = f_recyclerAdapterData[adapter] as MutableList<Badge>
            data.addAll(discordBadges)
        }

        // Set image url for badge
        patcher.after<UserProfileHeaderView.BadgeViewHolder>("bind", Badge::class.java)
        { (_, badge: Badge) ->
            val url = badge.objectType

            // Check that badge is ours (has icon = 0 and url set)
            if (badge.icon != 0 || url == null) return@after

            val binding = f_badgeViewHolderBinding[this] as UserProfileHeaderBadgeBinding
            val imageView = binding.b
            imageView.setCacheableImage(url)
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private companion object {
        val MANIFEST = Manifest(
            name = "DiscordBadges",
            description = "Displays newly added Discord profile badges",
        )
    }
}