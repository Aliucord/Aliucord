package com.aliucord.updater

import android.os.Build
import com.aliucord.*
import com.aliucord.entities.CorePlugin
import com.aliucord.entities.Plugin
import com.aliucord.settings.AUTO_UPDATE_PLUGINS_KEY
import com.aliucord.utils.SemVer
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Manages fetching and installing plugin updates.
 * This class is pure and does not store plugin updates globally.
 */
internal object PluginUpdater {
    private val logger = Logger("Updater/Plugins")

    /**
     * Represents an available plugin update.
     */
    data class PluginUpdate(
        /**
         * The currently loaded plugin this is update applies to.
         */
        val plugin: Plugin,
        /**
         * The plugin's manifest name/id
         */
        val pluginName: String = plugin.name,
        /**
         * The fetched update info for the latest build of this plugin.
         */
        val info: PluginUpdaterSource.PluginBuildInfo,
    ) {
        /**
         * Whether the base Discord/Aliucord installation is outdated and
         * requires a reinstallation update through Aliucord Manager.
         */
        val isBaseOutdated: Boolean = info.minimumDiscordVersion > Constants.DISCORD_VERSION ||
            !ManagerBuild.hasKotlin(info.minimumKotlinVersion.toString())

        /**
         * Whether the current Aliucord core is outdated and requires an update.
         */
        val isCoreOutdated: Boolean = (info.minimumAliucordVersion ?: SemVer.Zero) > SemVer.parse(BuildConfig.VERSION)

        /**
         * Whether the current Android version is too low to load the new plugin.
         */
        val isAndroidOutdated: Boolean = info.minimumApiLevel > Build.VERSION.SDK_INT

        /**
         * Whether this plugin should be allowed to update as
         * the new build will not cause issues upon loading.
         */
        fun isUpdatePossible(): Boolean =
            !isBaseOutdated && !isCoreOutdated && !isAndroidOutdated
    }

    /**
     * Determines whether automatic plugin updates have been enabled by the user.
     */
    @JvmStatic
    fun isAutoUpdateEnabled(): Boolean = Main.settings.getBool(AUTO_UPDATE_PLUGINS_KEY, false)

    /**
     * Force fetches all available updates, including ones that cannot be updated.
     * The resulting updates should not be held for long durations (ie, cached globally).
     */
    @JvmStatic
    fun fetchUpdates(source: PluginUpdaterSource): List<PluginUpdate> {
        logger.info("Checking for plugin updates...")

        val updates = mutableListOf<PluginUpdate>()
        val mainInfo = source.getMainManifestBuildInfo()
        val regex = Regex(
            """(?:cdn\.jsdelivr\.net/gh/|raw\.githubusercontent\.com/|github\.com/)([^/]+)/([^/@]+)"""
        )

        val urlsCache = ConcurrentHashMap<String, Pair<String, String>>()
        val comparisonCache = ConcurrentHashMap<Pair<String, String>, Boolean>()

        fun compareUrls(a: String?, b: String?): Boolean {
            if (a == null || b == null) return false

            val key = if (a <= b) a to b else b to a

            return comparisonCache.getOrPut(key) {
                fun parse(url: String): Pair<String, String>? {
                    return urlsCache.getOrPut(url) {
                        val m = regex.find(url, 0) ?: return@getOrPut null
                        m.groupValues[1] to m.groupValues[2]
                    }
                }

                val p1 = parse(a) ?: return@getOrPut false
                val p2 = parse(b) ?: return@getOrPut false
                p1 == p2
            }
        }

        // Check if a plugin has an update available and add it to the list
        fun checkAndAdd(plugin: Plugin, name: String, repo: Map<String, PluginUpdaterSource.PluginBuildInfo>?): Boolean {
            try {
                // Plugin not found in repo
                val info = repo?.get(name) ?: return false
                if (!compareUrls(info.repoUrl, plugin.manifest.updateUrl)) return false

                // Assume invalid local versions are always out of date
                val local = SemVer.parseOrNull(plugin.manifest.version) ?: SemVer.Zero

                // Plugin is already up-to-date
                if (local >= info.version) return true

                updates += PluginUpdate(plugin, name, info)
                return true
            } catch (e: Exception) {
                logger.error("Failed checking updates for ${plugin.name} (${plugin.__filename}.zip)", e)
                return false
            }
        }

        // Check main manifest first, group remaining plugins by their update URL for parallel fetching
        val fallback = mutableMapOf<String, MutableList<Pair<Plugin, String>>>()
        for (plugin in PluginManager.plugins.values) {
            if (plugin is CorePlugin) continue
            val name = plugin.manifest.name ?: continue
            val url = plugin.manifest.updateUrl?.takeIf(String::isNotEmpty) ?: continue
            if (!checkAndAdd(plugin, name, mainInfo)) fallback.getOrPut(url) { mutableListOf() }.add(plugin to name)
        }

        // Fetch all remaining repos in parallel
        val executor = Executors.newFixedThreadPool(4)
        try {
            fallback.map { (url, plugins) ->
                plugins to executor.submit<Map<String, PluginUpdaterSource.PluginBuildInfo>?> {
                    source.getRepoBuildInfo(url)
                }
            }.forEach { (plugins, future) ->
                val repo = future.get() ?: return@forEach
                plugins.forEach { (plugin, name) -> checkAndAdd(plugin, name, repo) }
            }
        } finally {
            executor.shutdown()
        }

        return updates
    }

    @JvmStatic
    fun updatePlugin(update: PluginUpdate): Boolean {
        if (!update.isUpdatePossible())
            throw IllegalArgumentException("Cannot perform plugin update that is not possible")

        return try {
            // Legacy build url style, which contains %s as a placeholder for the plugin name (optional now)
            val downloadUrl = update.info.buildUrl.replace("%s", update.pluginName)

            Http.Request(downloadUrl).execute().use { resp ->
                // TODO: verify crc32
                resp.saveToFile(File(Constants.PLUGINS_PATH, "${update.plugin.__filename}.zip"))
            }

            Utils.mainThread.post {
                PluginManager.remountPlugin(update.plugin.name)
            }
            true
        } catch (e: Exception) {
            logger.error("Failed to update plugin ${update.plugin} (${update.plugin.__filename}.zip)", e)
            false
        }
    }
}
