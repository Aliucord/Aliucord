package com.discord.models.domain;

import java.io.Closeable;
import java.io.IOException;

@SuppressWarnings("unused")
public interface Model {
    class JsonReader implements Closeable {
        public final com.google.gson.stream.JsonReader in;

        public JsonReader(com.google.gson.stream.JsonReader input) {
            in = input;
        }

        public void close() throws IOException { throw new IOException(); }
    }
}
