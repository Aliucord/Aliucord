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
    private var field: Field? = null

    override fun getValue(thisRef: T, property: KProperty<*>): Field {
        return field
            ?: clazz.getDeclaredField(
                fieldName ?: property.name.removeSuffix("Field")
            ).apply {
                isAccessible = true
                field = this
            }
    }
}

/**
 * A lazy method delegate designed to improve the performance in method reflection.
 *
 * @param clazz     The class that the method belongs to.
 * @param methodName The name of the method.
 */
class LazyMethod<T>(private val clazz: Class<*>, private val methodName: String?) : ReadOnlyProperty<T, Method> {
    private var method: Method? = null

    override fun getValue(thisRef: T, property: KProperty<*>): Method {
        return method
            ?: clazz.getDeclaredMethod(
                methodName ?: property.name
            ).apply {
                isAccessible = true
                method = this
            }
    }
}

/**
 * A delegate that provides efficient accessing of a field via reflection.
 *
 * @param T The type of the field.
 * @param fieldName The name of the field. If null, will use the property name.
 */
class FieldAccessor<T>(private val fieldName: String?): ReadWriteProperty<Any, T> {
    private val fields = mutableListOf<Field>()

    private fun field(thisRef: Any, property: KProperty<*>): Field {
        val clazz = thisRef::class.java
        return fields.find { it.declaringClass == clazz }
            ?: clazz.getDeclaredField(
                fieldName ?: property.name.removeSuffix("Field")
            ).apply {
                isAccessible = true
                fields.add(this)
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>) = field(thisRef, property)[thisRef] as T
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = field(thisRef, property).set(thisRef, value)
}

/**
 * A delegate that provides efficient accessing of a no-args getter via reflection.
 *
 * @param T The type of the value returned by the getter.
 * @param methodName The name of the getter. If null, will use the property name to form `getName`.
 */
class GetterAccessor<T>(private val methodName: String?) : ReadOnlyProperty<Any, T> {
    private val methods = mutableListOf<Method>()

    private fun method(thisRef: Any, property: KProperty<*>): Method {
        val clazz = thisRef::class.java
        return methods.find { it.declaringClass == clazz }
            ?: clazz.getDeclaredMethod(
                methodName ?: "get${property.name.replaceFirstChar { it.uppercaseChar() }}"
            ).apply {
                isAccessible = true
                methods.add(this)
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return method(thisRef, property).invoke(thisRef) as T
    }
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
 * @param T The type of the field to be accessed.
 * @param fieldName The name of the field. If null, will use the property name.
 */
fun <T> accessField(fieldName: String? = null) = FieldAccessor<T>(fieldName)

/**
 * A delegate that provides efficient accessing of a no-args getter via reflection.
 *
 * @param T The type of the value returned by the getter.
 * @param methodName The name of the getter. If null, will use the property name to form `getName`.
 */
fun <T> accessGetter(methodName: String? = null) = GetterAccessor<T>(methodName)
