/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import androidx.annotation.NonNull;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@SuppressWarnings("unused")
public class Http {
    public static class HttpException extends IOException {
        public HttpException(String message) {
            super(message);
        }
    }

    /** QueryString Builder */
    public static class QueryBuilder {
        private final StringBuilder sb;

        public QueryBuilder(String baseUrl) {
            sb = new StringBuilder(baseUrl + "?");
        }

        /**
         * Append query parameter. Will automatically be encoded for you
         * @param key The parameter key
         * @param value The parameter value
         * @return self
         */
        public QueryBuilder append(String key, String value) {
            try {
                key = URLEncoder.encode(key, "UTF-8");
                value = URLEncoder.encode(value, "UTF-8");
                sb.append(key).append('=').append(value).append('&');
            } catch (UnsupportedEncodingException ignored) {} // This should never happen
            return this;
        }

        /**
         * Build the finished Url
         */
        @NonNull
        public String toString() {
            String str = sb.toString();
            return str.substring(0, str.length() -1); // Remove last & or ? if no query specified
        }
    }

    /** Request Builder */
    public static class Request {
        /** The connection of this Request */
        public final HttpURLConnection conn;

        public Request(QueryBuilder builder) throws IOException {
            this(builder.toString(), "GET");
        }
        public Request(String url) throws IOException {
            this(url, "GET");
        }
        public Request(String url, String method) throws IOException {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method.toUpperCase());
            conn.addRequestProperty("User-Agent", "Aliucord (https://github.com/Aliucord/Aliucord)");
        }

        /**
         * Add a header
         * @param key the name
         * @param value the value
         * @return self
         */
        public Request setHeader(String key, String value) {
            conn.setRequestProperty(key, value);
            return this;
        }

        /**
         * Sets the request connection and read timeout
         * @param timeout the timeout, in milliseconds
         * @return self
         */
        public Request setRequestTimeout(int timeout) {
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            return this;
        }

        /**
         * Sets whether redirects should be followed
         * @param follow Whether redirects should be followed
         * @return self
         */
        public Request setFollowRedirects(boolean follow) {
            conn.setInstanceFollowRedirects(follow);
            return this;
        }

        /**
         * Execute the request
         * @return A response object
         */
        public Response execute() throws IOException {
            return new Response(this);
        }

        /**
         * Execute the request with the specified body. May not be used in GET requests.
         * @param body The request body
         * @return self
         */
        public Response executeWithBody(String body) throws IOException {
            if (conn.getRequestMethod().equals("GET")) throw new HttpException("Body may not be specified in GET requests");
            conn.setDoOutput(true);
            try (OutputStream out = conn.getOutputStream()) {
                byte[] bytes = body.getBytes();
                out.write(bytes, 0, bytes.length);
                out.flush();
            }
            return execute();
        }

        /**
         * Execute the request with the specified object as json. May not be used in GET requests.
         * @param body The request body
         * @return self
         */
        public Response executeWithJson(Object body) throws IOException {
            return setHeader("Content-Type", "application/json").executeWithBody(Utils.toJson(body));
        }
    }

    /** Response obtained by calling Request.execute() */
    public static class Response {
        private final Request req;
        /** The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">status code</a> of this response */
        public final int statusCode;
        /** The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">status message</a> of this response */
        public final String statusMessage;

        public Response(Request req) throws IOException {
            this.req = req;
            statusCode = req.conn.getResponseCode();
            statusMessage = req.conn.getResponseMessage();
        }

        /** Whether the request was successful (status code 2xx) */
        public boolean ok() {
            return statusCode > 199 && statusCode < 300;
        }

        /** Throws an HttpException if this request was not successful */
        public void assertOk() throws HttpException {
            if (!ok()) throw new HttpException(String.format("%d: %s", statusCode, statusMessage));
        }

        /** Get the raw response */
        public String text() throws IOException {
            String ln;
            StringBuilder res = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream()))) {
                while ((ln = reader.readLine()) != null) res.append(ln);
            }
            return res.toString().trim();
        }

        /**
         * Deserializes json response
         * @param type Class to deserialize into
         * @return Response Object
         */
        public <T> T json(Type type) throws IOException {
            return Utils.fromJson(text(), type);
        }

        /**
         * Get the raw response stream of this connection
         * @return InputStream
         */
        public InputStream stream() throws IOException {
            return req.conn.getInputStream();
        }

        /**
         * Pipe response into OutputStream. Remember to close the OutputStream
         * @param os The OutputStream to pipe into
         */
        public void pipe(OutputStream os) throws IOException {
            try (InputStream is = stream()) {
                int n;
                byte[] buf = new byte[16384]; // 16 KB
                while ((n = is.read(buf)) > -1) {
                    os.write(buf, 0, n);
                }
                os.flush();
            }
        }
    }

    /**
     * Send a simple GET request
     * @param url The url to fetch
     * @return Raw response (String). If you want Json, use simpleJsonGet
     */
    public static String simpleGet(String url) throws IOException {
        Response res = new Request(url, "GET").execute();
        res.assertOk();
        return res.text();
    }

    /**
     * Send a simple GET request
     * @param url The url to fetch
     * @param schema Class to <a href="https://en.wikipedia.org/wiki/Serialization">deserialize</a> the response into
     * @return Response Object
     */
    public static <T> T simpleJsonGet(String url, Type schema) throws IOException {
        String res = simpleGet(url);
        return Utils.fromJson(res, schema);
    }

    /**
     * Send a simple POST request
     * @param url The url to fetch
     * @param body The request body
     * @return Raw response (String). If you want Json, use simpleJsonPost
     */
    public static String simplePost(String url, String body) throws IOException {
        Response res = new Request(url, "POST").executeWithBody(body);
        res.assertOk();
        return res.text();
    }

    /**
     * Send a simple POST request and parse the JSON response
     * @param url The url to fetch
     * @param body The request body
     * @param schema Class to <a href="https://en.wikipedia.org/wiki/Serialization">deserialize</a> the response into
     * @return Response deserialized into the provided Class
     */
    public static <T> T simpleJsonPost(String url, String body, Type schema) throws IOException {
        String res = simplePost(url, body);
        return Utils.fromJson(res, schema);
    }

    /**
     * Send a simple POST request with JSON body
     * @param url The url to fetch
     * @param body The request body
     * @param schema Class to <a href="https://en.wikipedia.org/wiki/Serialization">deserialize</a> the response into
     * @return Response deserialized into the provided Class
     */
    public static <T> T simpleJsonPost(String url, Object body, Type schema) throws IOException {
        return new Request(url).executeWithJson(body).json(schema);
    }
}
