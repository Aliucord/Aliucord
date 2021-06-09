package com.discord.nullserializable;

import kotlin.jvm.internal.DefaultConstructorMarker;

public abstract class NullSerializable<T> {
    public static final class a<T> extends NullSerializable<T> {
        public final T b;

        public a() { this(null); }

        public a(T value) {
            super(null, null);
            b = value;
            if (value != null) throw new IllegalArgumentException("value must be null");
        }
    }

    public static final class b<T> extends NullSerializable<T> {
        public final T b;

        public b(T value) {
            super(value, null);
            b = value;
        }

        @Override
        public T a() { return b; }
    }

    public final T a;

    public NullSerializable(T value, DefaultConstructorMarker obj) {
        a = value;
    }

    public T a() { return a; }
}
