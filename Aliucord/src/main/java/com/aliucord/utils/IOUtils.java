/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import java.io.*;

public final class IOUtils {
    /**
     * Reads the InputStream into a byte[]
     * @param stream The stream to read
     * @return The read bytes
     * @throws Throwable if an I/O error occurs
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static byte[] readBytes(InputStream stream) throws Throwable {
        int len = stream.available();
        byte[] buf = new byte[len];
        stream.read(buf);
        stream.close();
        return buf;
    }

    /**
     * Pipe an {@link InputStream} into an {@link OutputStream}
     * @param is InputStream
     * @param os OutputStream
     * @throws IOException if an I/O error occurs
     */
    public static void pipe(InputStream is, OutputStream os) throws IOException {
        int n;
        byte[] buf = new byte[16384]; // 16 KB
        while ((n = is.read(buf)) > -1) {
            os.write(buf, 0, n);
        }
        os.flush();
    }
}
