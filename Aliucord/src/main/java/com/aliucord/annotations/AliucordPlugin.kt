/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.annotations

/**
 * Annotates the entrypoint of a plugin, used for the manifest.json generation
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@Suppress("unused")
annotation class AliucordPlugin(
    /**
     * The version code of your plugin, e.g. 1.0.0
     * Used by the updater to figure out if plugins are outdated.
     */
    val version: String = "",
    /**
     * The description of your plugin, will show on the plugin card in Settings > Plugins
     */
    val description: String = "",
    /**
     * Your plugin's changelog. Supports markdown
     */
    val changelog: String = "",
    /**
     * The banner that will show on top of the changelog screen
     */
    val changelogMedia: String = ""
)
