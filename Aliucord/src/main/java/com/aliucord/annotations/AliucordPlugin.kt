package com.aliucord.annotations

/**
 * Annotates the entrypoint of a plugin, used by manifest.json generation
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class AliucordPlugin(
    val version: String = "",
    val description: String = "",
    val changelog: String = "",
    val changelogMedia: String = ""
)