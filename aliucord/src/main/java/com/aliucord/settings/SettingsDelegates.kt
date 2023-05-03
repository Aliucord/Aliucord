package com.aliucord.settings

import com.aliucord.api.SettingsAPI
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.*

class SettingsDelegate<T>(
    private val defaultValue: T,
    private val settings: SettingsAPI
) : ReadWriteProperty<Any, T> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        settings.getUnknown(property.name, defaultValue) as T

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        settings.setUnknown(property.name, value)
}

fun <T> SettingsAPI.delegate(
    defaultValue: T
) = SettingsDelegate(defaultValue, this)
