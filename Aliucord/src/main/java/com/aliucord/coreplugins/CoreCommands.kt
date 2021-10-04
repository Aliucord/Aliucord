/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.coreplugins

import android.content.Context
import android.os.Build
import com.aliucord.BuildConfig
import com.aliucord.Constants
import com.aliucord.api.CommandsAPI
import com.aliucord.api.CommandsAPI.CommandResult
import com.discord.api.commands.ApplicationCommandType
import com.aliucord.PluginManager
import com.aliucord.entities.Plugin
import com.discord.models.commands.ApplicationCommandOption
import java.io.File

internal class CoreCommands : Plugin() {
    override fun start(context: Context) {
        commands.registerCommand(
            "echo",
            "Creates Clyde message",
            listOf(CommandsAPI.requiredMessageOption)
        ) {
            CommandResult(it.getRequiredString("message"), null, false)
        }

        commands.registerCommand(
            "say",
            "Sends message",
            listOf(CommandsAPI.requiredMessageOption)
        ) {
            CommandResult(it.getRequiredString("message"))
        }

        commands.registerCommand(
            "plugins",
            "Lists installed plugins",
            listOf(
                ApplicationCommandOption(
                    ApplicationCommandType.BOOLEAN,
                    "send",
                    "Whether the result should be visible for everyone",
                    null,
                    false,
                    false,
                    null,
                    null,
                    null,
                    false
                )
            )
        ) {
            val plugins = PluginManager.plugins.keys
            if (plugins.isEmpty())
                CommandResult("No plugins installed", null, false)
            else
                CommandResult(
                    "**Installed Plugins (${plugins.size}):**\n>>> ${
                        plugins.sorted().joinToString()
                    }",
                    null,
                    it.getBoolOrDefault("send", false)
                )
        }

        commands.registerCommand("debug", "Posts debug info", emptyList()) {
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