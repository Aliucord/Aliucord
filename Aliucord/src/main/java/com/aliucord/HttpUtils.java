/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {
    public static String stringRequest(String url, String body) throws IOException {
        String ln;
        StringBuilder res = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(request(url, body)));
        while ((ln = reader.readLine()) != null) res.append(ln);
        reader.close();

        return res.toString().trim();
    }

    public static InputStream request(String url, String body) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        boolean post = body != null;
        conn.setDoOutput(post);
        conn.setRequestMethod(post ? "POST" : "GET");
        if (post) {
            OutputStream out = conn.getOutputStream();
            byte[] bytes = body.getBytes();
            out.write(bytes, 0, bytes.length);
            out.flush();
            out.close();
        }

        return conn.getInputStream();
    }
}
