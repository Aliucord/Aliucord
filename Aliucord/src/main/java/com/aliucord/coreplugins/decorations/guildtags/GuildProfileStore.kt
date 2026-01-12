package com.aliucord.coreplugins.decorations.guildtags

import androidx.collection.LruCache
import com.aliucord.*
import com.aliucord.Http.HttpException
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.SerializedName
import com.discord.api.commands.Application

private val logger = Logger("Decorations/GuildTag")

data class GuildProfile(
    val id: Long,
    val name: String,
    val iconHash: String?,
    val memberCount: Int,
    val onlineCount: Int,
    val description: String?,
    val brandColorPrimary: String?,
    val gameApplicationIds: List<Long>,
    val gameActivity: HashMap<Long, GameActivity>,
    val tag: String?,
    val badge: Int,
    val badgeColorPrimary: String,
    val badgeColorSecondary: String,
    val badgeHash: String,
    val traits: List<Trait>,
    val features: List<String>,
    @SerializedName("visibility") private val _visibility: Int,
    val customBannerHash: String?,
    val premiumSubscriptionCount: Int,
    @SerializedName("premium_tier") private val _premiumTier: Int,
) {
    data class GameActivity(
        val activityLevel: Int,
        val activityScore: Int,
    )
    data class Trait(
        val emojiId: Long?,
        val emojiName: String?,
        val emojiAnimated: Boolean,
        val label: String,
        val position: Int,
    )

    enum class Visibility {
        PUBLIC,
        RESTRICTED,
        PUBLIC_WITH_RECRUITMENT,
    }

    enum class PremiumTier {
        NONE,
        TIER_1,
        TIER_2,
        TIER_3,
    }

    val visibility get() = when (_visibility) {
        1 -> Visibility.PUBLIC
        2 -> Visibility.RESTRICTED
        3 -> Visibility.PUBLIC_WITH_RECRUITMENT
        else -> {
            logger.warn("Unknown GuildProfile visibility $_visibility")
            Visibility.RESTRICTED
        }
    }

    val premiumTier get() = when (_premiumTier) {
        0 -> PremiumTier.NONE
        1 -> PremiumTier.TIER_1
        2 -> PremiumTier.TIER_2
        3 -> PremiumTier.TIER_3
        else -> {
            logger.warn("Unknown GuildProfile premium tier $_premiumTier")
            PremiumTier.NONE
        }
    }
}

object GuildProfileStore {
    private val cache = LruCache<Long, ProfileResult>(5)

    sealed class ProfileResult {
        data class Failed(val id: Long, val exception: HttpException) : ProfileResult()
        object Private : ProfileResult()
        data class Available(val profile: GuildProfile, val applications: List<Application>) : ProfileResult()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun get(id: Long, callback: (ProfileResult) -> Unit) {
        cache.get(id)?.let {
            callback(it)
            return
        }
        Utils.threadPool.execute {
            val res: ProfileResult
            val profileReq = Http.Request.newDiscordRNRequest("/guilds/${id}/profile")
            val profileRes = profileReq.execute()

            if (profileRes.statusCode == 403) {
                res = ProfileResult.Private
            } else if (!profileRes.ok()) {
                res = ProfileResult.Failed(id, HttpException(profileReq, profileRes))
            } else {
                val profile = profileRes.json(GsonUtils.gsonRestApi, GuildProfile::class.java)
                val applications = if (profile.gameApplicationIds.isNotEmpty()) {
                    val query = Http.QueryBuilder("/applications/public")
                    for (appId in profile.gameApplicationIds) {
                        query.append("application_ids", appId.toString())
                    }
                    Http.Request.newDiscordRNRequest(query)
                        .execute()
                        .json(GsonUtils.gsonRestApi, Array<Application>::class.java)
                        .toList()
                } else {
                    listOf()
                }

                res = ProfileResult.Available(profile, applications)
            }

            if (res !is ProfileResult.Failed) {
                cache.put(id, res)
            }

            Utils.mainThread.post {
                callback(res)
            }
        }
    }
}
