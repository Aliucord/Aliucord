@file:Suppress("unused")
package com.aliucord.settings

import com.aliucord.api.SettingsAPI
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SettingsDelegate<T>(
    private val defaultValue: T
) : ReadWriteProperty<SettingsAPI, T> {

    override fun getValue(`this`: SettingsAPI, property: KProperty<*>): T {
        return `this`.getObject(property.name, defaultValue)
    }

    override fun setValue(`this`: SettingsAPI, property: KProperty<*>, value: T) {
        return `this`.setObject(property.name, value)
    }
}

fun <T> SettingsAPI.delegate(
    defaultValue: T
) = SettingsDelegate(defaultValue)
