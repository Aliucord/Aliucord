/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.coreplugins

import android.content.Context
import android.net.Uri
import android.os.Build
import com.aliucord.*
import com.aliucord.api.CommandsAPI
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.aliucord.settings.Crashes
import com.aliucord.updater.PluginUpdater
import com.aliucord.updater.Updater
import com.aliucord.utils.RxUtils.subscribe
import com.discord.api.commands.ApplicationCommandType
import com.discord.app.AppLog
import com.discord.app.AppLog.LoggedItem
import external.org.apache.commons.lang3.StringUtils
import org.json.JSONObject
import java.io.*
import java.util.*

internal class CoreCommands : Plugin() {
    init {
        Manifest().run {
            name = "CoreCommands"
            initialize(this)
        }
    }

    override fun start(context: Context) {
        commands.registerCommand(
            "echo",
            "Creates Clyde message",
            CommandsAPI.requiredMessageOption
        ) {
            CommandResult(it.getRequiredString("message"), null, false)
        }

        commands.registerCommand(
            "say",
            "Sends message",
            CommandsAPI.requiredMessageOption
        ) {
            CommandResult(it.getRequiredString("message"))
        }

        fun formatPlugins(plugins: List<Plugin>, showVersions: Boolean, joiner: String = ", ", showOutdated: Boolean = false): String =
            StringBuilder().run {
                plugins.forEach {
                    append(it.getName())
                    if (showVersions) append(" (").append(it.manifest.version).append(')')
                    if (showOutdated && PluginUpdater.checkPluginUpdate(it)) append(" (Outdated)")
                    append(joiner)
                }
                setLength(length - joiner.length) // Remove last joiner
                toString()
            }

        commands.registerCommand(
            "plugins",
            "Lists installed plugins",
            listOf(
                Utils.createCommandOption(
                    type = ApplicationCommandType.BOOLEAN,
                    name = "send",
                    description = "Whether the result should be visible for everyone",
                ),
                Utils.createCommandOption(
                    type = ApplicationCommandType.BOOLEAN,
                    name = "versions",
                    description = "Whether to show the plugin versions",
                )
            )
        ) {
            val showVersions = it.getBoolOrDefault("versions", false)

            val plugins = PluginManager.plugins
            val (enabled, disabled) = plugins.values.partition(PluginManager::isPluginEnabled)
            val enabledStr = formatPlugins(enabled, showVersions)
            val disabledStr = formatPlugins(disabled, showVersions)

            if (plugins.isEmpty())
                CommandResult("No plugins installed", null, false)
            else
                CommandResult(
                    """
**Enabled Plugins:**
${if (enabled.isEmpty()) "None" else "> $enabledStr"}
**Disabled Plugins:**
${if (disabled.isEmpty()) "None" else "> $disabledStr"}
                """,
                    null,
                    it.getBoolOrDefault("send", false)
                )
        }

        commands.registerCommand("debug", "Posts debug info") {
            // .trimIndent() is broken sadly due to collision with Discord's Kotlin
            val str = """
**Debug Info:**
> Discord: ${Constants.DISCORD_VERSION}
> Aliucord: ${BuildConfig.GIT_REVISION} (${PluginManager.plugins.size} plugins)
> System: Android ${Build.VERSION.RELEASE} (SDK v${Build.VERSION.SDK_INT}) - ${getArchitecture()}
> Rooted: ${getIsRooted() ?: "Unknown"}
            """

            CommandResult(str)
        }

        AppLog.d.subscribe {
            if (this.k != 2) Utils.debugLogs.add(this) //this is for preventing http request logs from getting saved
            if (Utils.debugLogs.size > 400) {
                Utils.debugLogs.removeFirst()
            }
        }
        commands.registerCommand("doctor", "Posts crash logs with device info") {
            val plugins = PluginManager.plugins
            val (enabled, disabled) = plugins.values.partition(PluginManager::isPluginEnabled)
            val enabledStr = formatPlugins(enabled, true, "\n  • ", showOutdated = true)
            val disabledStr = formatPlugins(disabled, true, "\n  • ", showOutdated = true)
            val crashes = Crashes.getCrashes()?.filter {
                it.value.timestampmilis > Calendar.getInstance().timeInMillis - 3600 * 1000
            }
            val debugLog = StringBuilder()
            (Utils.debugLogs.clone() as ArrayList<LoggedItem>).forEach {
                val indentLevel = "\n" + StringUtils.repeat("\t", it.k - 3)
                debugLog.append(indentLevel + it.l)
                if (it.k == 6 && it.m != null) { //level 6 is error and it.m is throwable
                    val sw = StringWriter()
                    it.m.printStackTrace(PrintWriter(sw))
                    val exceptionAsString: String = sw.toString()
                    debugLog.append(exceptionAsString.trim().replace("\n", indentLevel))
                }
            }
            val res = StringBuilder()
            crashes?.forEach { res.append(it.value.timestamp + "\n" + it.value.stacktrace) }

            if (res.isEmpty()) res.append("No Crashes")
            val info =
                """
❯ Discord: ${Constants.DISCORD_VERSION} ${if (Updater.isDiscordOutdated()) " (Outdated)" else ""}
❯ Aliucord: ${BuildConfig.GIT_REVISION} (${PluginManager.plugins.size} plugins) ${if (Updater.isAliucordOutdated()) " (Outdated)" else ""}
❯ System: Android ${Build.VERSION.RELEASE} (SDK v${Build.VERSION.SDK_INT}) - ${getArchitecture()}
❯ Rooted: ${getIsRooted() ?: "Unknown"}
❯ Device: ${Build.DEVICE}
❯ Model: ${Build.MODEL}
❯ Manufacturer: ${Build.MANUFACTURER}

❯ Enabled Plugins (${enabled.size})
${if (enabled.isEmpty()) "None" else "  • $enabledStr"}

❯ Disabled Plugins (${disabled.size})
${if (disabled.isEmpty()) "None" else "  • $disabledStr"}

❯ Recent Crashlogs

$res

❯ Debug Log
${debugLog.trim()}
"""
            if (info.length > 400000) {
                val file = File.createTempFile("doctor_temp", null)
                file.writeText(info)
                file.deleteOnExit()
                it.addAttachment(Uri.fromFile(file).toString(), "doctor.txt")
                CommandResult("")
            } else {
                val key = JSONObject(Http.simplePost("https://haste.powercord.dev/documents", info)).get("key")
                CommandResult("https://haste.powercord.dev/$key")
            }
        }
    }

    private fun getIsRooted() =
        System.getenv("PATH")?.split(':')?.any {
            File(it, "su").exists()
        }

    private fun getArchitecture(): String {
        Build.SUPPORTED_ABIS.forEach {
            when (it) {
                "arm64-v8a" -> return "aarch64"
                "armeabi-v7a" -> return "arm"
                "x86_64" -> return "x86_64"
                "x86" -> return "i686"
            }
        }
        return System.getProperty("os.arch") ?: System.getProperty("ro.product.cpu.abi")
        ?: "Unknown Architecture"
    }

    override fun stop(context: Context) {}
}
