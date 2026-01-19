package com.aliucord.updater

import android.content.ActivityNotFoundException
import android.content.Intent
import com.aliucord.*
import com.aliucord.api.NotificationsAPI
import com.aliucord.entities.NotificationData
import com.aliucord.fragments.ConfirmDialog
import com.aliucord.settings.ALIUCORD_FROM_STORAGE_KEY
import com.aliucord.settings.AUTO_UPDATE_ALIUCORD_KEY
import com.aliucord.updater.CoreUpdater.UPDATER_DATA_URL
import com.aliucord.utils.SemVer
import com.aliucord.utils.SerializedName

/**
 * Handles checking for core/base updates and updating the Aliucord core itself.
 */
internal object CoreUpdater {
    private val logger = Logger("Updater/Core")

    /**
     * Fetches the remote build info.
     */
    private fun fetchAliucordData(): BuildData {
        return Http.simpleJsonGet(UPDATER_DATA_URL, BuildData::class.java)
    }

    /**
     * Checks for base & core updates, and updates the core if possible.
     */
    @JvmStatic
    fun checkForUpdates() {
        if (isUpdaterDisabled() || isCustomCoreEnabled()) return

        try {
            logger.debug("Checking for Aliucord updates...")
            val data = fetchAliucordData()

            if (data.coreVersion > SemVer.parse(BuildConfig.VERSION)) {
                if (Constants.DISCORD_VERSION < data.discordVersion
                    || !ManagerBuild.hasKotlin(data.kotlinVersion.toString())
                    || !ManagerBuild.hasInjector(data.kotlinVersion.toString())
                    || !ManagerBuild.hasPatches(data.patchesVersion.toString())
                ) {
                    val notificationData = NotificationData()
                        .setTitle("Updater")
                        .setBody("This installation is outdated!\n" +
                            "Click to reinstall Aliucord using Aliucord Manager...")
                        .setAutoDismissPeriodSecs(30)
                        .setOnClick { reinstallAliucord() }

                    NotificationsAPI.display(notificationData)
                } else if (!isAutoUpdateEnabled()) {
                    val notificationData = NotificationData()
                        .setTitle("Updater")
                        .setBody("Aliucord has an update available!\n" +
                            "Click to automatically update...")
                        .setAutoDismissPeriodSecs(30)
                        // TODO: open Updater screen instead once it support showing core updates
                        .setOnClick { updateAliucord() }

                    NotificationsAPI.display(notificationData)
                } else {
                    updateAliucord()
                }
            } else {
                logger.debug("No updates found!")
            }
        } catch (e: Exception) {
            logger.errorToast("Failed to check updates for Aliucord", e)
        }
    }

    /**
     * Launches manager to perform an update installation.
     * If manager is not installed, prompt to install it.
     */
    private fun reinstallAliucord() {
        val intent = Intent("com.aliucord.manager.REINSTALL")
            .setClassName("com.aliucord.manager", "com.aliucord.manager.MainActivity")
            .putExtra("aliucord.packageName", Utils.appContext.packageName)

        try {
            Utils.appActivity.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            ConfirmDialog()
                .setTitle("Updater")
                .setDescription("""
                    Aliucord Manager is not installed on this device.
                    Open Github releases?
                    """.trimIndent())
                .setOnOkListener { Utils.launchUrl("https://github.com/Aliucord/Manager/releases/latest") }
                .show(Utils.appActivity.supportFragmentManager, "No Manager")
        }
    }

    /**
     * Forcefully replaces the local Aliucord version with the latest from Github.
     * This does not perform any checks as to whether it is safe to do so.
     */
    private fun updateAliucord() = Utils.threadPool.execute {
        try {
            logger.debug("Downloading new core bundle from $CORE_ZIP_URL...")
            Http.simpleDownload(CORE_ZIP_URL, Utils.appContext.codeCacheDir.resolve("Aliucord.zip"))
            logger.debug("Finished downloading core")

            Utils.promptRestart("Aliucord update requires a restart. Restart now?")
        } catch (e: Exception) {
            logger.errorToast("Failed to update Aliucord!", e)
        }
    }

    /**
     * Determines whether the updater is disabled
     *
     * @return Whether preference "disableAliucordUpdater" is set to true
     */
    @JvmStatic
    fun isUpdaterDisabled(): Boolean = Main.settings.getBool("disableAliucordUpdater", false)

    /**
     * Determines whether automatic core updates have been disabled by the user.
     */
    @JvmStatic
    fun isAutoUpdateEnabled(): Boolean = Main.settings.getBool(AUTO_UPDATE_ALIUCORD_KEY, false)

    /**
     * Determines whether the Aliucord dex is being loaded from storage
     *
     * @return Whether preference [ALIUCORD_FROM_STORAGE_KEY] is set to true
     */
    @JvmStatic
    fun isCustomCoreEnabled(): Boolean {
        // FIXME: this is still true even when custom core doesn't exist
        return Main.settings.getBool(ALIUCORD_FROM_STORAGE_KEY, false)
    }

    /**
     * The model of the data available at [UPDATER_DATA_URL].
     */
    private data class BuildData(
        @SerializedName("versionCode")
        var discordVersion: Int,
        var coreVersion: SemVer,
        var injectorVersion: SemVer,
        var patchesVersion: SemVer,
        var kotlinVersion: SemVer,
    )

    private const val UPDATER_DATA_URL = "https://builds.aliucord.com/data.json"
    private const val CORE_ZIP_URL = "https://builds.aliucord.com/Aliucord.zip"
}
