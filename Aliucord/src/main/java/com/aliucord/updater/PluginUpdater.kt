package com.aliucord.updater

import android.os.Build
import com.aliucord.*
import com.aliucord.entities.CorePlugin
import com.aliucord.entities.Plugin
import com.aliucord.settings.AUTO_UPDATE_PLUGINS_KEY
import com.aliucord.utils.SemVer
import java.io.File

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
        for (plugin in PluginManager.plugins.values) {
            try {
                if (plugin is CorePlugin) continue

                val info = source.getPluginBuildInfo(
                    pluginName = plugin.manifest.name ?: continue,
                    updateInfoUrl = plugin.manifest.updateUrl
                        ?.takeIf { it.isNotEmpty() }
                        ?: continue,
                )
                if (info == null) {
                    logger.warn("Failed to check updates for plugin ${plugin.name} (${plugin.__filename}.zip)")
                    continue
                }

                // Plugin is already up-to-date
                if (SemVer.parse(plugin.manifest.version) >= info.version)
                    continue

                updates += PluginUpdate(
                    plugin = plugin,
                    pluginName = plugin.name,
                    info = info,
                )
            } catch (e: Exception) {
                logger.error("Failed checking updates for plugin ${plugin.name} (${plugin.__filename}.zip)", e)
                continue
            }
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

            reloadPlugin(update.plugin)
            true
        } catch (e: Exception) {
            logger.error("Failed to update plugin ${update.plugin} (${update.plugin.__filename}.zip)", e)
            false
        }
    }

    private fun reloadPlugin(plugin: Plugin) {
        // FIXME: plugin not reloaded when disabled
        if (!PluginManager.isPluginEnabled(plugin.name)) return

        Utils.mainThread.post {
            PluginManager.remountPlugin(plugin.name)
            val newPlugin = PluginManager.plugins[plugin.name] ?: return@post

            // FIXME: only prompt once when updating all plugins at once
            if (plugin.requiresRestart() || newPlugin.requiresRestart()) {
                Utils.promptRestart("Plugin update requires a restart. Restart now?")
            }
        }
    }
}
