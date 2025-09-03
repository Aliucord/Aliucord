package com.aliucord.settings

import com.aliucord.api.SettingsAPI
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SettingsDelegate<T>(
    private val defaultValue: T,
    private val name: String?,
    private val settings: SettingsAPI
) : ReadWriteProperty<Any, T> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        settings.getUnknown(name ?: property.name, defaultValue) as T

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
        settings.setUnknown(name ?: property.name, value)
}

fun <T> SettingsAPI.delegate(
    defaultValue: T
) = SettingsDelegate(defaultValue, null, this)

fun <T> SettingsAPI.delegate(
    name: String,
    defaultValue: T,
) = SettingsDelegate(defaultValue, name, this)
