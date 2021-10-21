/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.plugindownloader

internal data class PluginInfo(val version: String, val minimumDiscordVersion: Int)
internal data class PluginCardInfo(val pluginFile: PluginFile, val title: String, val exists: Boolean, val minimumDiscordVersion: Int)
