package com.aliucord.annotations

/**
 * Annotates the entrypoint of a plugin, used by manifest.json generation
 */
@Suppress("unused")
@Target(AnnotationTarget.CLASS)
annotation class AliucordPlugin(
    /**
     * Prompts the user to restart Aliucord after:
     * - Enabling manually / dropping into plugins folder
     * - Disabling manually
     * - Updating
     */
    val requiresRestart: Boolean = false
)
