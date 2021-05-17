package com.google.gson.reflect;

import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class TypeToken<T> {
    public final Type getType() { return null; }

    public static TypeToken<?> getParameterized(Type type, Type... types) { return new TypeToken<>(); }
}
