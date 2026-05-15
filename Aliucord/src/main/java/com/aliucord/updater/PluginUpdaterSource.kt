package com.aliucord.updater

import com.aliucord.Http
import com.aliucord.Logger
import com.aliucord.utils.*
import com.aliucord.utils.GsonUtils.fromJson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Handles fetching and caching update info from plugin repositories.
 */
internal class PluginUpdaterSource {
    private val logger = Logger("Updater/Plugins/Source")
    private val rawGithubRegex =
        "https://raw\\.githubusercontent\\.com/(.+?)/(.+?)/(.+)".toRegex()

    /**
     * A TTL cache of updater data from plugin repositories.
     * This maps the **original** plugin update info url to the fetched JSON.
     */
    private val cachedRepoInfo = ConcurrentHashMap<String, RepoBuildInfo>()

    /**
     * Retrieves the update info for a plugin repository.
     * @return Record of plugin name -> plugin updater info, or `null` if data failed to fetch.
     */
    fun getRepoBuildInfo(updateInfoUrl: String): Map<String, PluginBuildInfo>? {
        val url = updateInfoUrl.replace(rawGithubRegex, "https://cdn.jsdelivr.net/gh/$1/$2@$3")

        cachedRepoInfo[url]?.let {
            if (System.currentTimeMillis() < it.expiration) return it.plugins
            cachedRepoInfo.remove(url)
        }

        val json = fetchJson<Map<String, PluginBuildInfo>>(url)
        cachedRepoInfo[url] = RepoBuildInfo(plugins = json)
        return json
    }

    fun getMainManifestBuildInfo(): Map<String, PluginBuildInfo>? {
        return fetchJson<List<PluginBuildInfo>>("https://plugins.aliucord.com/manifest.json")
            ?.associateBy { it.name!! }
    }

    private inline fun <reified T> fetchJson(url: String, type: Type = object : TypeToken<T>() {}.type): T? {
        try {
            val resp = Http.Request(url).execute()
            if (!resp.ok()) {
                logger.warn("Failed to fetch $url, Status ${resp.statusCode}, Response: ${resp.text()}")
                return null
            }
            return GsonUtils.gson.fromJson(resp.text(), type)
        } catch (e: Exception) {
            logger.warn("Failed to fetch plugin update info from $url", e)
            return null
        }
    }

    /**
     * Latest build info for a specific plugin.
     */
    data class PluginBuildInfo(
        var name: String? = null,
        var version: SemVer,
        var repoUrl: String? = null,
        @SerializedName("build", alternate = ["url"])
        var buildUrl: String,
        var buildCrc32: String? = null,
        var changelog: String? = null,
        var changelogMedia: String? = null,
        var minimumDiscordVersion: Int = 0,
        var minimumAliucordVersion: SemVer? = null,
        @SerializedName("minimumKotlinVersion")
        var minimumKotlinVersionInternal: SemVer? = null,
        // Default used to be 24 before this property was introduced
        var minimumApiLevel: Int = 24,
    ) {
        // Default used to be 1.5.21 before this property was introduced
        val minimumKotlinVersion: SemVer
            get() = SemVer(1, 5, 21)
    }

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
