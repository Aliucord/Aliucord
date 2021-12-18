/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.coreplugins

import android.content.Context
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import c.d.b.a.a
import com.aliucord.*
import com.aliucord.api.CommandsAPI
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.aliucord.settings.Crashes
import com.aliucord.utils.ReflectUtils
import com.aliucord.utils.RxUtils.subscribe
import com.discord.api.commands.ApplicationCommandType
import com.discord.app.*
import com.discord.utilities.rest.RestAPI
import com.discord.widgets.debugging.WidgetDebugging
import com.discord.widgets.debugging.`WidgetDebugging$onViewBoundOrOnResume$3`
import org.json.JSONObject
import java.io.File
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

        fun formatPlugins(plugins: List<Plugin>, showVersions: Boolean): String =
            plugins.joinToString(transform = { p -> p.getName() + if (showVersions) " (${p.manifest.version})" else "" })

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

        commands.registerCommand("doctor", "Posts crash logs with device info") {
            val plugins = PluginManager.plugins
            val (enabled, disabled) = plugins.values.partition(PluginManager::isPluginEnabled)
            val enabledStr = formatPlugins(enabled, true)
            val disabledStr = formatPlugins(disabled, true)
            val crashes = Crashes.getCrashes()?.filter {
                it.value.timestampmilis > Calendar.getInstance().timeInMillis - 3600 * 1000
            }
            val res = StringBuilder()
            crashes?.forEach { res.append(it.value.timestamp + "\n" + it.value.stacktrace) }
            if (res.isEmpty()) res.append("No Crashes")

            val info =
                """ **Doctor**
> Discord: ${Constants.DISCORD_VERSION}
> Aliucord: ${BuildConfig.GIT_REVISION} (${PluginManager.plugins.size} plugins)
> System: Android ${Build.VERSION.RELEASE} (SDK v${Build.VERSION.SDK_INT}) - ${getArchitecture()}
> Rooted: ${getIsRooted() ?: "Unknown"}
> Device: ${Build.DEVICE}
> Model: ${Build.MODEL}
> Manufacturer: ${Build.MANUFACTURER}

**Enabled Plugins**
${if (enabled.isEmpty()) "None" else "/ " + enabledStr.replace(",", "\n/")}

**Disabled Plugins**
${if (disabled.isEmpty()) "None" else "/ " + disabledStr.replace(",", "\n/")}

**Crash Reports From Last 1 hour**

$res
"""
            val key = JSONObject(Http.simplePost("https://www.hb.vendicated.dev/documents", info)).get("key")
            CommandResult("https://www.hb.vendicated.dev/$key")
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
