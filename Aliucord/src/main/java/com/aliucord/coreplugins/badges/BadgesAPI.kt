package com.aliucord.coreplugins.badges

import com.aliucord.*
import com.aliucord.api.SettingsAPI
import com.aliucord.settings.delegate
import kotlin.time.ExperimentalTime
import kotlin.time.days
import kotlin.time.hours

internal class BadgesAPI(private val settings: SettingsAPI) {
    private var SettingsAPI.cacheExpiration by settings.delegate(0L)
    private var SettingsAPI.cachedBadges by settings.delegate(BadgesInfo(emptyMap(), emptyMap()))

    /**
     * Get cached badge data or re-fetch from the API if expired.
     */
    @Suppress("DEPRECATION")
    @OptIn(ExperimentalTime::class)
    fun getBadges(): BadgesInfo {
        if (!isCacheExpired()) return settings.cachedBadges

        val data = fetchBadges()

        return if (data != null) {
            settings.cacheExpiration = System.currentTimeMillis() + 1.days.inWholeMilliseconds
            settings.cachedBadges = data
            data
        } else {
            // Failed to fetch; keep cache and try later
            settings.cacheExpiration = System.currentTimeMillis() + 6.hours.inWholeMilliseconds
            settings.cachedBadges
        }
    }

    /**
     * Fetch badge data from the API directly.
     */
    private fun fetchBadges(): BadgesInfo? {
        return try {
            Http.Request("https://aliucord.com/files/badges/data.json")
                .setHeader("User-Agent", "Aliucord/${BuildConfig.VERSION}")
                .execute()
                .json(BadgesInfo::class.java)
        } catch (e: Exception) {
            Logger("BadgesAPI").error("Failed to fetch supporter badges!", e)
            null
        }
    }

    private fun isCacheExpired(): Boolean =
        settings.cacheExpiration <= System.currentTimeMillis()
}

private typealias Snowflake = Long
private typealias BadgeName = String

internal data class BadgesInfo(
    val guilds: Map<Snowflake, BadgeData>,
    val users: Map<Snowflake, BadgesUserData>,
)

internal data class BadgesUserData(
    val roles: List<BadgeName>?,
    val custom: List<BadgeData>?,
)

internal data class BadgeData(
    val url: String,
    val text: String,
)
