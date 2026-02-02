package com.aliucord.updater

import com.aliucord.Logger
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.GsonUtils.fromJson
import com.aliucord.utils.SemVer

/**
 * Version checking for various install-time utilities that were used to install the app.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object ManagerBuild {
    var metadata: InstallMetadata? = null
        private set

    /**
     * Whether this installation has *at least* a specific version of smali patches applied to it.
     */
    @JvmStatic
    fun hasPatches(version: String): Boolean =
        (metadata?.patchesVersion ?: SemVer.Zero) >= SemVer.parse(version)

    /**
     * Whether this installation has *at least* a specific version of injector added to it.
     */
    @JvmStatic
    fun hasInjector(version: String): Boolean =
        (metadata?.injectorVersion ?: SemVer.Zero) >= SemVer.parse(version)

    /**
     * Whether this installation has *at least* a specific version of the Kotlin stdlib added to it.
     */
    @JvmStatic
    fun hasKotlin(version: String): Boolean =
        (metadata?.kotlinVersion ?: SemVer(1, 5, 21)) >= SemVer.parse(version)

    /**
     * Whether this installation was patched with *at least* a specific version of manager
     * @param version If null, then any version matches.
     */
    @JvmStatic
    fun installedWithManager(version: String?): Boolean {
        return if (version == null) {
            metadata != null
        } else {
            (metadata?.managerVersion ?: SemVer.Zero) >= SemVer.parse(version)
        }
    }

    init {
        try {
            // Manager adds this to the APK root
            val stream = this.javaClass.classLoader?.getResourceAsStream("aliucord.json")

            if (stream != null) {
                metadata = GsonUtils.gson.fromJson(
                    stream.bufferedReader(),
                    InstallMetadata::class.java
                )
            }
        } catch (e: Exception) {
            Logger("ManagerBuild").warn("Failed to parse Manager install metadata", e)
        }
    }

    data class InstallMetadata(
        val customManager: Boolean,
        val managerVersion: SemVer,
        val injectorVersion: SemVer,
        val patchesVersion: SemVer,
        val kotlinVersion: SemVer? = null,
    )
}
