package com.aliucord.settings

import com.aliucord.api.SettingsAPI
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class SettingsDelegate<T> internal constructor(
    private val defaultValue: T,
    private val name: String?,
    private val settings: SettingsAPI,
) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        settings.getUnknown(name ?: property.name, defaultValue) as T

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
        settings.setUnknown(name ?: property.name, value)

    // Refer to the deprecation notice below!
    private fun getName(): String =
        name ?: throw IllegalStateException("Delegate was not initialized with a name!")

    /**
     * Manually set the setting value without binding this delegate to a field.
     */
    var value: T
        get() = settings.getUnknown(getName(), defaultValue) as T
        set(value) = settings.setUnknown(getName(), value)
}

@Deprecated(
    message = "You should pass the setting's internal name directly!",
    replaceWith = ReplaceWith("settings.delegate(\"<NAME>\", defaultValue)"),
)
fun <T> SettingsAPI.delegate(
    defaultValue: T
) = SettingsDelegate(defaultValue, null, this)

fun <T> SettingsAPI.delegate(
    name: String,
    defaultValue: T,
) = SettingsDelegate(defaultValue, name, this)
