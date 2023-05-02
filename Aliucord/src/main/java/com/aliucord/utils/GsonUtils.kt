/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils

import com.discord.models.domain.Model
import com.google.gson.Gson
import java.io.Reader
import java.lang.reflect.Type
import b.a.b.a as TypeAdapterRegistrar
import b.i.d.c as FieldNamingPolicy
import b.i.d.e as GsonBuilder

object GsonUtils {
    /** [Gson](https://github.com/google/gson) instance */
    @JvmStatic
    val gson = Gson()

    /** [Gson](https://github.com/google/gson) instance with pretty print enabled */
    @JvmStatic
    val gsonPretty = Gson().apply {
        // set pretty print to true
        ReflectUtils.setField(this, "k", true)
    }

    /** [Gson](https://github.com/google/gson) instance with same config as Discord uses for [com.discord.utilities.rest.RestAPI] */
    @JvmStatic
    val gsonRestApi: Gson = GsonBuilder().run {
        c = FieldNamingPolicy.m // LOWER_CASE_WITH_UNDERSCORES
        TypeAdapterRegistrar.a(this)
        e.add(Model.TypeAdapterFactory())
        a()
    }

    /**
     * Deserializes a JSON string into the specified class
     * @param json The JSON string to deserialize
     * @param clazz The class to deserialize the JSON into
     * @return Deserialized JSON
     */
    @JvmStatic
    fun <T> Gson.fromJson(json: String?, clazz: Class<T>): T = g(json, clazz)

    @JvmStatic
    @Deprecated("Use kt extension for Gson", ReplaceWith("gson.fromJson(json, clazz)"))
    fun <T> fromJson(json: String?, clazz: Class<T>): T = gson.fromJson(json, clazz)

    /**
     * Deserializes a JSON string into the specified class
     * @param reader The reader from which JSON will be deserialized
     * @param clazz The class to deserialize the JSON into
     * @return Deserialized JSON
     */
    @JvmStatic
    fun <T> Gson.fromJson(reader: Reader?, clazz: Class<T>): T = e(reader, clazz)

    @JvmStatic
    @Deprecated("Use kt extension for Gson", ReplaceWith("gson.fromJson(reader, clazz)"))
    fun <T> fromJson(reader: Reader?, clazz: Class<T>) = gson.fromJson(reader, clazz)

    /**
     * Deserializes a JSON string into the specified object
     * @param json The JSON string to deserialize
     * @param type The type of the object to deserialize the JSON into
     * @return Deserialized JSON
     */
    @JvmStatic
    fun <T> Gson.fromJson(json: String?, type: Type?): T = g(json, type)

    @JvmStatic
    @Deprecated("Use kt extension for Gson", ReplaceWith("gson.fromJson(json, type)"))
    fun <T> fromJson(json: String?, type: Type?): T = gson.fromJson(json, type)

    /**
     * Serializes an Object to JSON
     * @param obj The object to serialize
     * @return Serialized JSON
     */
    @JvmStatic
    fun Gson.toJson(obj: Any?): String = m(obj)

    @JvmStatic
    @Deprecated("Use kt extension for Gson", ReplaceWith("gson.toJson(obj)"))
    fun toJson(obj: Any?): String = gson.toJson(obj)

    /**
     * Serializes an Object to pretty printed JSON
     * @param obj The object to serialize
     * @return Serialized JSON
     */
    @JvmStatic
    @Deprecated("Use kt extension for Gson", ReplaceWith("gsonPretty.toJson(obj)"))
    fun toJsonPretty(obj: Any?): String = gsonPretty.toJson(obj)
}
