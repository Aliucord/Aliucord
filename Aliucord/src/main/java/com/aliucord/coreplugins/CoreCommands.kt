/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.coreplugins

import android.content.Context
import android.os.Build
import com.aliucord.BuildConfig
import com.aliucord.Constants
import com.aliucord.PluginManager
import com.aliucord.Utils
import com.aliucord.api.CommandsAPI
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.discord.api.commands.ApplicationCommandType
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
                    name = "allPlugins",
                    description = "Whether the result should contain all plugins",
                )
            )
        ) {
            val allPlugins = it.getBoolOrDefault("allPlugins", false)
            val type = if (allPlugins) "installed" else "enabled"
            val plugins = PluginManager.plugins.keys.run {
                if (!allPlugins) filter { pluginName -> PluginManager.isPluginEnabled(pluginName) } else this
            }

            if (plugins.isEmpty())
                CommandResult("No plugins $type", null, false)
            else
                CommandResult(
                    "**${type.replaceFirstChar { char -> char.uppercase() }} Plugins (${plugins.size}):**\n>>> ${plugins.sorted().joinToString()}",
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