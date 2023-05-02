package com.aliucord.annotations

/**
 * Annotates the entrypoint of a plugin, used by manifest.json generation
 */
@Target(AnnotationTarget.CLASS)
annotation class AliucordPlugin(
    /**
     * Prompts the user to restart Aliucord after:
     * - Enabling manually
     * - Disabling manually
     * - Updating
     * - Uninstalling
     */
    val requiresRestart: Boolean = false
)
