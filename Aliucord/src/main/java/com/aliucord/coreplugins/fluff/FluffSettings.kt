package com.aliucord.coreplugins.fluff

import com.aliucord.api.SettingsAPI
import com.aliucord.settings.delegate

internal object FluffSettings {
    private val settings = SettingsAPI("Fluff")

    // TODO: remove this
    // This exists to allow enabling for testing by manually editing the json settings
    val enable by settings.delegate(false)
}
