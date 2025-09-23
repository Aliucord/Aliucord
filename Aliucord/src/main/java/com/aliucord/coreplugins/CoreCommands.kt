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
import com.aliucord.updater.ManagerBuild
import com.discord.api.commands.ApplicationCommandType
import java.io.File

internal class CoreCommands : CorePlugin(Manifest("CoreCommands")) {
    init {
        manifest.description = "Adds basic slash commands to Aliucord for debugging purposes"
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
            val (enabled, disabled) = PluginManager.getVisiblePlugins().values.partition(PluginManager::isPluginEnabled)

            fun formatPlugins(plugins: List<Plugin>): String {
                return plugins.joinToString { p ->
                    if (showVersions && p !is CorePlugin) {
                        "${p.name} (${p.manifest.version})"
                    } else if (p is CorePlugin) {
                        "*${p.name}*"
                    } else {
                        p.name
                    }
                }
            }

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
            // .trimIndent() is broken sadly due to collision with Discord's Kotlin
            val managerBuildInfo = ManagerBuild.metadata?.run {
                """- Built with **Manager** $managerVersion ${if (customManager) "(Custom)" else ""}
> **Injector**: $injectorVersion
> **Patches**: $patchesVersion"""
            } ?: ""

            var str = """
**Debug Info:**
> **Discord**: ${Constants.DISCORD_VERSION}
> **Aliucord**: ${BuildConfig.VERSION} ${if (BuildConfig.RELEASE) "" else "(Custom)"} $managerBuildInfo
> **Plugins**: ${PluginManager.getPluginsInfo()}
> **Android**: ${Build.VERSION.RELEASE} (SDK v${Build.VERSION.SDK_INT}) - ${getArchitecture()} - ${Build.PRODUCT}
> **Rooted**: ${getIsRooted() ?: "Unknown"}
            """

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val manifest = File("/apex/com.android.art/apex_manifest.pb").takeIf { it.exists() }
                    ?.readBytes()

                str += "> **ART manifest version**: ${manifest?.let { ProtobufParser.getField2(it) } ?: "Unknown"}"
            }

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

private object ProtobufParser {
    private fun parseVarInt(data: ByteArray, offset: Int): Pair<Long, Int> {
        var result = 0L
        var pos = offset
        var shift = 0

        while (true) {
            val byte = data[pos++].toInt() and 0xFF
            result = result or ((byte and 0x7F).toLong() shl shift)
            if (byte and 0x80 == 0) break
            shift += 7
        }

        return result to pos
    }

    fun getField2(data: ByteArray): Long? {
        var offset = 0

        while (offset < data.size) {
            val tag = data[offset++].toInt() and 0xFF
            if (tag shr 3 == 2) return parseVarInt(data, offset).first
            offset = when (tag and 0x07) {
                0 -> parseVarInt(data, offset).second
                1 -> offset + 8
                2 -> parseVarInt(data, offset).let { (len, off) -> off + len.toInt() }
                else -> return null
            }
        }

        return null
    }
}
