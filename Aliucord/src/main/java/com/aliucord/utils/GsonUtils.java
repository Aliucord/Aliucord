/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import com.aliucord.Main;
import com.google.gson.Gson;

import java.io.Reader;
import java.lang.reflect.Type;

public final class GsonUtils {
    /** <a href="https://github.com/google/gson">Gson</a> instance */
    public static final Gson gson = new Gson();
    /** Pretty <a href="https://github.com/google/gson">Gson</a> instance */
    public static final Gson gsonPretty = new Gson();

    /**
     * Deserializes a JSON string into the specified class
     * @param json The JSON string to deserialize
     * @param clazz The class to deserialize the JSON into
     * @return Deserialized JSON
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.g(json, clazz);
    }

    /**
     * Deserializes a JSON string into the specified class
     * @param reader The reader from which JSON will be deserialized
     * @param clazz The class to deserialize the JSON into
     * @return Deserialized JSON
     */
    public static <T> T fromJson(Reader reader, Class<T> clazz) {
        return gson.e(reader, clazz);
    }

    /**
     * Deserializes a JSON string into the specified object
     * @param json The JSON string to deserialize
     * @param type The type of the object to deserialize the JSON into
     * @return Deserialized JSON
     */
    public static <T> T fromJson(String json, Type type) {
        return gson.g(json, type);
    }

    /**
     * Serializes an Object to JSON
     * @param obj The object to serialize
     * @return Serialized JSON
     */
    public static String toJson(Object obj) {
        return gson.m(obj);
    }
    /**
     * Serializes an Object to pretty printed JSON
     * @param obj The object to serialize
     * @return Serialized JSON
     */
    public static String toJsonPretty(Object obj) {
        return gsonPretty.m(obj);
    }

    static {
        try {
            // set pretty print to true
            ReflectUtils.setField(gsonPretty, "k", true);
        } catch (Throwable e) { Main.logger.error(e); }
    }
}
