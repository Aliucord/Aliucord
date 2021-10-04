/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import androidx.annotation.NonNull;

import java.io.*;
import java.nio.charset.Charset;

public final class IOUtils {
    /**
     * Reads the {@link InputStream} as text
     * @param is The input stream to read
     * @return The text
     * @throws IOException if an I/O error occurs
     */
    @NonNull
    public static String readAsText(@NonNull InputStream is) throws IOException {
        try (var baos = new ByteArrayOutputStream(is.available())) {
            pipe(is, baos);
            return baos.toString("UTF-8");
        }
    }

    /**
     * Reads the InputStream into a <code>byte[]</code>
     * @param stream The stream to read
     * @return The read bytes
     * @throws IOException if an I/O error occurs
     */
    @NonNull
    public static byte[] readBytes(@NonNull InputStream stream) throws IOException {
        try (var baos = new ByteArrayOutputStream(stream.available())) {
            pipe(stream, baos);
            return baos.toByteArray();
        }
    }

    /**
     * Pipe an {@link InputStream} into an {@link OutputStream}
     * @param is InputStream
     * @param os OutputStream
     * @throws IOException if an I/O error occurs
     */
    public static void pipe(@NonNull InputStream is, @NonNull OutputStream os) throws IOException {
        int n;
        byte[] buf = new byte[16384]; // 16 KB
        while ((n = is.read(buf)) > -1) {
            os.write(buf, 0, n);
        }
        os.flush();
    }
}
