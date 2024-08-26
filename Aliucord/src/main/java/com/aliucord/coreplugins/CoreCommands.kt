/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.coreplugins

import android.content.Context
import android.os.Build
import com.aliucord.*
import com.aliucord.api.CommandsAPI
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.CorePlugin
import com.aliucord.entities.Plugin
import com.discord.api.commands.ApplicationCommandType
import java.io.File

internal class CoreCommands : CorePlugin(Manifest("CoreCommands")) {
    init {
        manifest.description = "Adds basic slash commands to Aliucord for debugging purposes"
    }

    private fun visiblePlugins(): Sequence<Plugin> {
        return PluginManager.plugins
            .values.asSequence()
            .filter { it !is CorePlugin || !it.isHidden }
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
            val (enabled, disabled) = visiblePlugins().partition(PluginManager::isPluginEnabled)

            fun formatPlugins(plugins: List<Plugin>): String =
                plugins.joinToString { p -> if (showVersions && p !is CorePlugin) "${p.name} (${p.manifest.version})" else p.name }

            if (enabled.isEmpty() && disabled.isEmpty())
                CommandResult("No plugins installed", null, false)
            else
                CommandResult(
                    """
**Enabled Plugins (${enabled.size}):**
${if (enabled.isEmpty()) "None" else "> ${formatPlugins(enabled)}"}
**Disabled Plugins (${disabled.size}):**
${if (disabled.isEmpty()) "None" else "> ${formatPlugins(disabled)}"}
                """,
                    null,
                    it.getBoolOrDefault("send", false)
                )
        }

        commands.registerCommand("debug", "Posts debug info") {
            val customPluginCount = PluginManager.plugins.values.count { it !is CorePlugin }
            val enabledPluginCount = visiblePlugins().count(PluginManager::isPluginEnabled)

            // .trimIndent() is broken sadly due to collision with Discord's Kotlin
            val str = """
**Debug Info:**
> Discord: ${Constants.DISCORD_VERSION}
> Aliucord: ${BuildConfig.VERSION} ${if (BuildConfig.RELEASE) "" else "(Custom)"}
> Plugins: $customPluginCount installed, $enabledPluginCount total enabled
> System: Android ${Build.VERSION.RELEASE} (SDK v${Build.VERSION.SDK_INT}) - ${getArchitecture()}
> Rooted: ${getIsRooted() ?: "Unknown"}
            """

            CommandResult(str)
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
        return System.getProperty("os.arch")
            ?: System.getProperty("ro.product.cpu.abi")
            ?: "Unknown Architecture"
    }

    override fun stop(context: Context) {
        commands.unregisterAll()
    }
}
