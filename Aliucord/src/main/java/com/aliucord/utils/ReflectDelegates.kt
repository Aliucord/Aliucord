/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils

import java.lang.reflect.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A lazy field delegate designed to improve the performance in field reflection.
 *
 * @param clazz     The class that the field belongs to.
 * @param fieldName The name of the field.
 */
class LazyField<T>(private val clazz: Class<*>, private val fieldName: String?) : ReadOnlyProperty<T, Field> {
    private var v = null as Field?
    override fun getValue(thisRef: T, property: KProperty<*>) = v ?: clazz.getDeclaredField(fieldName ?: property.name.replace("Field", "")).apply {
        setAccessible(true)
        v = this
    }
}

/**
 * A lazy field delegate designed to improve the performance in field reflection.
 *
 * @param fieldName The name of the field.
 */
inline fun <reified T> lazyField(fieldName: String? = null) = LazyField<Any>(T::class.java, fieldName)
