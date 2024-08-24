package com.aliucord.entities

internal abstract class CorePlugin(manifest: Manifest) : Plugin(manifest) {
    /**
     * Hides this core plugin from the plugins page.
     * This should generally be used for coreplugins that are fixing existing functionality.
     */
    open val isHidden: Boolean = false

    /**
     * Whether this core plugin cannot be disabled at all.
     */
    open val isRequired: Boolean = false
}
