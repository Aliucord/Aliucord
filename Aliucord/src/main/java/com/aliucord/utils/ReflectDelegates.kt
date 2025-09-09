/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils

import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
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
        isAccessible = true
        v = this
    }
}

/**
 * A lazy method delegate designed to improve the performance in method reflection.
 *
 * @param clazz     The class that the method belongs to.
 * @param methodName The name of the method.
 */
class LazyMethod<T>(private val clazz: Class<*>, private val methodName: String?) : ReadOnlyProperty<T, Method> {
    private var v = null as Method?
    override fun getValue(thisRef: T, property: KProperty<*>) = v ?: clazz.getDeclaredMethod(methodName ?: property.name).apply {
        isAccessible = true
        v = this
    }
}

/**
 * A delegate that provides efficient accessing of a field via reflection.
 *
 * @param T The type of the class holding the field.
 * @param U The type of the field.
 * @param clazz The class holding the field.
 * @param fieldName The name of the field. If null, will use the property name.
 */
class FieldAccessor<T, U>(private val clazz: Class<T>, private val fieldName: String?): ReadWriteProperty<T, U> {
    private var field: Field? = null
    private fun field(property: KProperty<*>): Field {
        field?.let { return it }

        return clazz.getDeclaredField(
            fieldName ?: property.name.replace("Field", "")
        ).apply {
            isAccessible = true
            field = this
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: T, property: KProperty<*>) = field(property)[thisRef] as U
    override fun setValue(thisRef: T, property: KProperty<*>, value: U) = field(property).set(thisRef, value)
}

/**
 * A delegate that provides efficient accessing of a no-args getter via reflection.
 *
 * @param T The type of the class holding the getter.
 * @param U The type of the value returned by the getter.
 * @param clazz The class holding the getter.
 * @param methodName The name of the getter. If null, will use the property name to form `getName`.
 */
class GetterAccessor<T, U>(private val clazz: Class<T>, private val methodName: String?) : ReadOnlyProperty<T, U> {
    private var method = null as Method?
    private fun method(property: KProperty<*>): Method {
        method?.let { return it }

        return clazz.getDeclaredMethod(
            methodName ?: "get${property.name.replaceFirstChar { it.uppercaseChar() }}"
        ).apply {
            isAccessible = true
            method = this
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: T, property: KProperty<*>) = method(property).invoke(thisRef) as U
}

/**
 * A lazy field delegate designed to improve the performance in field reflection.
 *
 * @param fieldName The name of the field.
 */
inline fun <reified T> lazyField(fieldName: String? = null) = LazyField<Any>(T::class.java, fieldName)

/**
 * A lazy method delegate designed to improve the performance in method reflection.
 *
 * @param methodName The name of the method.
 */
inline fun <reified T> lazyMethod(methodName: String? = null) = LazyMethod<Any>(T::class.java, methodName)

/**
 * A delegate that provides efficient accessing of a field via reflection.
 *
 * @param T The type of the class holding the getter.
 * @param U The type of the value returned by the getter.
 * @param fieldName The name of the field. If null, will use the property name.
 */
inline fun <reified T, U> accessField(fieldName: String? = null) = FieldAccessor<T, U>(T::class.java, fieldName)

/**
 * A delegate that provides efficient accessing of a no-args getter via reflection.
 *
 * @param T The type of the class holding the getter.
 * @param U The type of the value returned by the getter.
 * @param methodName The name of the getter. If null, will use the property name to form `getName`.
 */
inline fun <reified T, U> accessGetter(methodName: String? = null) = GetterAccessor<T, U>(T::class.java, methodName)
