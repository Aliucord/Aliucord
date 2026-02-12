package com.aliucord.updater

import com.aliucord.Http
import com.aliucord.Logger
import com.aliucord.entities.Plugin
import com.aliucord.utils.*
import com.aliucord.utils.GsonUtils.fromJson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Handles fetching and caching update info from plugin repositories.
 */
internal object PluginRepoUpdater {
    private val logger = Logger("Updater/Plugins")

    /**
     * A TTL cache of updater data from plugin repositories.
     * This maps the **original** plugin update info url to the fetched json.
     */
    private val cachedRepoInfo = ConcurrentHashMap<String, RepoBuildInfo>()

    /**
     * Clears the update info cache
     */
    // TODO: this should be cleared after not being used for a while
    fun clear() {
        cachedRepoInfo.clear()
    }

    /**
     * Retrieves the update info for a specific plugin.
     * @param pluginName The plugin's manifest name.
     * @param updateInfoUrl The plugin's repository's update info url. [Plugin.Manifest.updateUrl]
     */
    fun getPluginBuildInfo(pluginName: String, updateInfoUrl: String): PluginBuildInfo? {
        return getRepoBuildInfo(updateInfoUrl)?.get(pluginName)
    }

    /**
     * Retrieves the update info for a plugin repository.
     * @return Record of plugin name -> plugin updater info, or `null` if data failed to fetch.
     */
    fun getRepoBuildInfo(updateInfoUrl: String): Map<String, PluginBuildInfo>? {
        // Replace raw.githubusercontent.com urls with jsdelivr to avoid ratelimiting
        val realUpdateInfoUrl = updateInfoUrl.replaceFirst(
            "https://raw\\.githubusercontent\\.com/(.+?)/(.+?)/(.+)",
            "https://cdn.jsdelivr.net/gh/$1/$2@$3"
        )

        cachedRepoInfo[realUpdateInfoUrl]?.let {
            if (System.currentTimeMillis() < it.expiration) {
                return it.plugins
            } else {
                cachedRepoInfo.remove(realUpdateInfoUrl)
            }
        }

        try {
            val resp = Http.Request(realUpdateInfoUrl).execute()

            // If update url doesn't exist, don't retry
            if (resp.statusCode == 404) {
                cachedRepoInfo[realUpdateInfoUrl] = RepoBuildInfo(plugins = null)
            }

            if (!resp.ok()) {
                logger.warn("Failed to fetch plugin update info from $realUpdateInfoUrl, " +
                    "Status ${resp.statusCode}, " +
                    "Response: ${resp.text()}")
                return null
            }

            val jsonType = TypeToken.getParameterized(Map::class.java, String::class.java, PluginBuildInfo::class.java).getType()
            val json = GsonUtils.gson.fromJson<Map<String, PluginBuildInfo>>(resp.text(), jsonType)

            cachedRepoInfo[realUpdateInfoUrl] = RepoBuildInfo(plugins = json)
            return json
        } catch (e: Exception) {
            cachedRepoInfo[realUpdateInfoUrl] = RepoBuildInfo(plugins = null)
            logger.warn("Failed to fetch plugin update info from $realUpdateInfoUrl", e)
            return null
        }
    }

    /**
     * Latest build info for a specific plugin.
     */
    data class PluginBuildInfo(
        var version: SemVer,
        @SerializedName("build")
        var buildUrl: String,
        var buildCrc32: String? = null,
        var changelog: String? = null,
        var changelogMedia: String? = null,
        var minimumDiscordVersion: Int = 0,
        var minimumAliucordVersion: SemVer? = null,
        // Default used to be 1.5.21 before this property was introduced
        var minimumKotlinVersion: SemVer = SemVer(1, 5, 21),
        // Default used to be 24 before this property was introduced
        var minimumApiLevel: Int = 24,
    )

    /**
     * A record of fetched build data for all plugins in a plugin repository.
     */
    data class RepoBuildInfo(
        /**
         * Record of plugin name -> updater info.
         * If null, then this request failed and should not be retried.
         */
        val plugins: Map<String, PluginBuildInfo>?,
        /**
         * A unix timestamp at which point this data should
         * be considered expired and be refetched.
         */
        val expiration: Long = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10),
    )
}
