package com.aliucord.utils

import java.lang.reflect.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LazyField<T>(private val clazz : Class<*>, private val field: String? ) : ReadOnlyProperty<T, Field> {
    private var v = null as Field?
    override fun getValue(thisRef: T, property: KProperty<*>) = v ?: clazz.getDeclaredField(field ?: property.name.replace("Field", "")).apply {
        setAccessible(true)
        v = this
    }
}

inline fun <reified T> lazyField(field: String? = null) = LazyField<Any>(T::class.java, field)