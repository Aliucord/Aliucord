/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliucord.utils.*;
import com.discord.stores.StoreStream;
import com.discord.utilities.analytics.AnalyticSuperProperties;
import com.discord.utilities.rest.RestAPI;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** Http Utilities */
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class Http {
    public static class HttpException extends IOException {
        /** The url of this request */
        public final URL url;
        /** The HTTP method of this request */
        public final String method;
        /** The status code of the response */
        public final int statusCode;
        /** The status message of the response */
        public final String statusMessage;
        /** The raw Request object */
        public final Request req;
        /** The raw Response object */
        public final Response res;

        /** Creates a new HttpException for the specified Request and Response */
        public HttpException(Request req, Response res) {
            super();
            this.req = req;
            this.res = res;
            this.statusCode = res.statusCode;
            this.statusMessage = res.statusMessage;
            this.method = req.conn.getRequestMethod();
            this.url = req.conn.getURL();
        }

        private String message;

        @Override
        @NonNull
        public String getMessage() {
            if (message == null) {
                var sb = new StringBuilder()
                    .append(res.statusCode)
                    .append(": ")
                    .append(res.statusMessage)
                    .append(" (")
                    .append(req.conn.getURL())
                    .append(')');

                try (var eis = req.conn.getErrorStream()) {
                    var s = IOUtils.readAsText(eis);
                    sb.append('\n').append(s);
                } catch (Throwable ignored) {
                }

                message = sb.toString();
            }
            return message;
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
         *
         * @param key   The parameter key
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
            return str.substring(0, str.length() - 1); // Remove last & or ? if no query specified
        }
    }

    /** Utility to build MultiPart requests */
    public static class MultiPartBuilder implements Closeable {
        private static final String LINE_FEED = "\r\n";
        private static final String PREFIX = "--";

        private final OutputStream outputStream;
        private final byte[] boundary;

        /**
         * @deprecated Use {@link #MultiPartBuilder(String, OutputStream)}
         */
        @Deprecated
        public MultiPartBuilder(@NonNull String boundary) {
            this(boundary, new ByteArrayOutputStream());
        }

        /**
         * Construct a new MultiPartBuilder writing to the provided OutputStream
         *
         * @param boundary Boundary
         * @param os       OutputStream to write to. Should optimally be the result of connection.getOutputStream()
         */
        public MultiPartBuilder(@NonNull String boundary, @NonNull OutputStream os) {
            this.boundary = boundary.getBytes(StandardCharsets.UTF_8);
            outputStream = os;
        }

        private MultiPartBuilder append(String s) throws IOException {
            return append(s.getBytes(StandardCharsets.UTF_8));
        }

        private MultiPartBuilder append(byte[] b) throws IOException {
            outputStream.write(b);
            return this;
        }

        /**
         * Append file. Will automatically be encoded for you
         *
         * @param fieldName  The parameter field name
         * @param uploadFile The parameter file
         * @return self
         */
        @NonNull
        public MultiPartBuilder appendFile(@NonNull String fieldName, @NonNull File uploadFile) throws IOException {
            append(PREFIX).append(boundary).append(LINE_FEED);
            append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(uploadFile.getName()).append("\"")
                .append(LINE_FEED);
            append("Content-Type: ").append(URLConnection.guessContentTypeFromName(uploadFile.getName()))
                .append(LINE_FEED);
            append("Content-Transfer-Encoding: binary").append(LINE_FEED);
            append(LINE_FEED);
            outputStream.flush();

            try (var inputStream = new FileInputStream(uploadFile)) {
                IOUtils.pipe(inputStream, outputStream);
            }

            append(LINE_FEED);
            outputStream.flush();

            return this;
        }


        /**
         * Append InputStream. Will automatically be encoded for you
         *
         * @param fieldName The parameter field name
         * @param is        The parameter stream
         * @return self
         */
        @NonNull
        public MultiPartBuilder appendStream(@NonNull String fieldName, @NonNull InputStream is) throws IOException {
            append(PREFIX).append(boundary).append(LINE_FEED);
            append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"").append(LINE_FEED);
            append("Content-Transfer-Encoding: binary").append(LINE_FEED);
            append(LINE_FEED);
            outputStream.flush();

            IOUtils.pipe(is, outputStream);

            append(LINE_FEED);
            outputStream.flush();

            return this;
        }

        /**
         * Append field. Will automatically be encoded for you
         *
         * @param fieldName The parameter field name
         * @param value     The parameter value
         * @return self
         */
        @NonNull
        public MultiPartBuilder appendField(@NonNull String fieldName, @NonNull String value) throws IOException {
            append(PREFIX).append(boundary).append(LINE_FEED);
            append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"").append(LINE_FEED);
            append(LINE_FEED);
            append(value).append(LINE_FEED);
            outputStream.flush();

            return this;
        }

        /**
         * Build the finished byte array
         *
         * @deprecated This method is only supported on ByteArrayOutputStreams. However, a HTTPUrlConnection OutputStream should be passed instead.
         */
        @NonNull
        @Deprecated
        public byte[] getBytes() throws UnsupportedOperationException, IOException {
            if (outputStream instanceof ByteArrayOutputStream) {
                finish();

                return ((ByteArrayOutputStream) outputStream).toByteArray();
            }
            throw new UnsupportedOperationException("getBytes is only supported on ByteArrayOutputStream builders");
        }

        /**
         * Finishes this MultiPartForm. This should be called last.
         * Calling any other methods on this Builder after calling this will lead to undefined behaviour.
         */
        public void finish() throws IOException {
            append(PREFIX).append(boundary).append(PREFIX).append(LINE_FEED);
            outputStream.flush();
            close();
        }

        @Override
        public void close() throws IOException {
            outputStream.close();
        }
    }

    /** Request Builder */
    public static class Request implements Closeable {
        /** The connection of this Request */
        public final HttpURLConnection conn;

        /**
         * Builds a GET request with the specified QueryBuilder
         *
         * @param builder QueryBuilder
         * @throws IOException If an I/O exception occurs
         */
        public Request(QueryBuilder builder) throws IOException {
            this(builder.toString(), "GET");
        }

        /**
         * Builds a GET request with the specified url
         *
         * @param url Url
         * @throws IOException If an I/O exception occurs
         */
        public Request(String url) throws IOException {
            this(url, "GET");
        }

        /**
         * Builds a request with the specified url and method
         *
         * @param url    Url
         * @param method <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods">HTTP method</a>
         * @throws IOException If an I/O exception occurs
         */
        public Request(String url, String method) throws IOException {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method.toUpperCase());
            conn.addRequestProperty("User-Agent", "Aliucord (https://github.com/Aliucord/Aliucord)");
        }

        /**
         * Add a header
         *
         * @param key   the name
         * @param value the value
         * @return self
         */
        public Request setHeader(String key, String value) {
            conn.setRequestProperty(key, value);
            return this;
        }

        /**
         * Sets the request connection and read timeout
         *
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
         *
         * @param follow Whether redirects should be followed
         * @return self
         */
        public Request setFollowRedirects(boolean follow) {
            conn.setInstanceFollowRedirects(follow);
            return this;
        }

        /**
         * Execute the request
         *
         * @return A response object
         */
        public Response execute() throws IOException {
            return new Response(this);
        }

        /**
         * Execute the request with the specified body. May not be used in GET requests.
         *
         * @param body The request body
         * @return Response
         */
        @NonNull
        public Response executeWithBody(@NonNull String body) throws IOException {
            if (conn.getRequestMethod().equals("GET")) throw new IOException("Body may not be specified in GET requests");

            byte[] bytes = body.getBytes();
            return executeWithBody(bytes);
        }

        /**
         * Execute the request with the specified raw bytes. May not be used in GET requests.
         *
         * @param bytes The request body in raw bytes
         * @return Response
         */
        @NonNull
        public Response executeWithBody(byte[] bytes) throws IOException {
            if (conn.getRequestMethod().equals("GET")) throw new IOException("Body may not be specified in GET requests");

            setHeader("Content-Length", Integer.toString(bytes.length));
            conn.setDoOutput(true);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(bytes, 0, bytes.length);
                out.flush();
            }

            return execute();
        }

        /**
         * Execute the request with the specified object as json. May not be used in GET requests.
         *
         * @param body The request body
         * @return Response
         */
        public Response executeWithJson(Object body) throws IOException {
            return setHeader("Content-Type", "application/json").executeWithBody(GsonUtils.toJson(body));
        }

        /**
         * Execute the request with the specified object as
         * <a href="https://url.spec.whatwg.org/#application/x-www-form-urlencoded">url encoded form data</a>.
         * May not be used in GET requests.
         *
         * @param params the form data
         * @return Response
         * @throws IOException if an I/O exception occurred
         */
        public Response executeWithUrlEncodedForm(Map<String, Object> params) throws IOException {
            QueryBuilder qb = new QueryBuilder("");
            for (Map.Entry<String, Object> entry : params.entrySet())
                qb.append(entry.getKey(), Objects.toString(entry.getValue()));

            return setHeader("Content-Type", "application/x-www-form-urlencoded").executeWithBody(qb.toString().substring(1));
        }

        /**
         * Execute the request with the specified object as multipart form-data. May not be used in GET requests.
         * <p>
         * Please note that this will set the <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Transfer-Encoding">Transfer-Encoding</a> to chunked.
         * Some servers may not support this. To upload un-chunked (will lead to running out of memory when uploading large files), call
         * <code>executeWithMultipartForm(params, false)</code>
         *
         * @param params Map of params. These will be converted in the following way:
         *               <ul>
         *                   <li>File: Append filename and content-type, then append the bytes of the file</li>
         *                   <li>InputStream: Read the stream fully and append the bytes</li>
         *                   <li>Other: Objects.toString() and append</li>
         *               </ul>
         * @return Response
         * @throws IOException if an I/O exception occurred
         */
        @NonNull
        public Response executeWithMultipartForm(@NonNull Map<String, Object> params) throws IOException {
            return executeWithMultipartForm(params, true);
        }

        /**
         * @param doChunkedUploading Whether to upload in chunks. If this is false, a buffer will be allocated to hold the entire
         *                           multi part form. When uploading large files this way, you will run out of memory. Not every server
         *                           supports this, also does not support redirects.
         * @see #executeWithMultipartForm(Map)
         */
        public Response executeWithMultipartForm(@NonNull Map<String, Object> params, boolean doChunkedUploading) throws IOException {
            if (conn.getRequestMethod().equals("GET")) throw new IOException("MultiPartForm may not be specified in GET requests");

            if (doChunkedUploading) {
                conn.setChunkedStreamingMode(-1);
            }

            final String boundary = "--" + UUID.randomUUID().toString() + "--";

            setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setDoOutput(true);

            try (var builder = new MultiPartBuilder(boundary, conn.getOutputStream())) {
                for (var entry : params.entrySet()) {
                    if (entry.getValue() instanceof File)
                        builder.appendFile(entry.getKey(), (File) entry.getValue());
                    else if (entry.getValue() instanceof InputStream)
                        builder.appendStream(entry.getKey(), (InputStream) entry.getValue());
                    else
                        builder.appendField(entry.getKey(), Objects.toString(entry.getValue()));
                }
                builder.finish();
                return execute();
            }
        }

        /** Closes this request */
        @Override
        public void close() {
            conn.disconnect();
        }

        /**
         * Performs a GET request to a Discord route
         *
         * @param builder QueryBuilder
         * @throws IOException If an I/O exception occurs
         */
        public static Request newDiscordRequest(QueryBuilder builder) throws IOException {
            return newDiscordRequest(builder.toString(), "GET");
        }

        /**
         * Performs a GET request to a Discord route
         *
         * @param route A Discord route, such as `/users/@me`
         * @throws IOException If an I/O exception occurs
         */
        public static Request newDiscordRequest(String route) throws IOException {
            return newDiscordRequest(route, "GET");
        }

        /**
         * Performs a request to a Discord route
         *
         * @param route  A Discord route, such as `/users/@me`
         * @param method <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods">HTTP method</a>
         * @throws IOException If an I/O exception occurs
         */
        public static Request newDiscordRequest(String route, String method) throws IOException {
            Request req = new Request(getDiscordRoute(route), method);
            req.setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.getUserAgent())
                .setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.getSuperPropertiesStringBase64())
                .setHeader("Accept", "*/*")
                .setHeader("Authorization", RestAPI.AppHeadersProvider.INSTANCE.getAuthToken());
            return req;
        }

        /**
         * Performs a GET request to a Discord route using RN headers
         *
         * @param builder QueryBuilder
         * @throws IOException If an I/O exception occurs
         */
        public static Request newDiscordRNRequest(QueryBuilder builder) throws IOException {
            return newDiscordRNRequest(builder.toString(), "GET");
        }

        /**
         * Performs a request to a Discord route using RN headers
         *
         * @param route A Discord route, such as `/users/@me`
         * @throws IOException If an I/O exception occurs
         */
        public static Request newDiscordRNRequest(String route) throws IOException {
            return newDiscordRNRequest(route, "GET");
        }

        /**
         * Performs a request to a Discord route using RN headers
         *
         * @param route  A Discord route, such as `/users/@me`
         * @param method <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods">HTTP method</a>
         * @throws IOException If an I/O exception occurs
         */
        public static Request newDiscordRNRequest(String route, String method) throws IOException {
            var req = new Request(getDiscordRoute(route), method);
            var headersProvider = RestAPI.AppHeadersProvider.INSTANCE;
            req.setHeader("User-Agent", "Discord-Android/" + RNSuperProperties.versionCode + ";RNA")
                .setHeader("X-Super-Properties", RNSuperProperties.getSuperPropertiesBase64())
                .setHeader("Accept-Language", headersProvider.getAcceptLanguages())
                .setHeader("Accept", "*/*")
                .setHeader("Authorization", headersProvider.getAuthToken())
                .setHeader("X-Discord-Locale", headersProvider.getLocale());
            return req;
        }

        private static String getDiscordRoute(String route) {
            return route.startsWith("http") ? route : "https://discord.com/api/v9" + route;
        }
    }

    /** Response obtained by calling Request.execute() */
    public static class Response implements Closeable {
        private final Request req;
        /** The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">status code</a> of this response */
        public final int statusCode;
        /** The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">status message</a> of this response */
        public final String statusMessage;

        /**
         * Construct a Response
         *
         * @param req The http request to execute
         * @throws IOException If an error occurred connecting to the server
         */
        public Response(Request req) throws IOException {
            this.req = req;
            statusCode = req.conn.getResponseCode();
            statusMessage = req.conn.getResponseMessage();
        }

        /** Whether the request was successful (status code 2xx) */
        public boolean ok() {
            return statusCode >= 200 && statusCode < 300;
        }

        /** Throws an HttpException if this request was unsuccessful */
        public void assertOk() throws HttpException {
            if (!ok()) throw new HttpException(req, this);
        }

        /** Get the response body as String */
        public String text() throws IOException {
            try (var is = stream()) {
                return IOUtils.readAsText(is);
            }
        }

        /** Get the response body as <code>byte[]</code> */
        public byte[] getBytes() throws IOException {
            try (var is = stream()) {
                return IOUtils.readBytes(is);
            }
        }

        /**
         * Deserializes json response
         *
         * @param type Type to deserialize into
         * @return Response Object
         */
        public <T> T json(Type type) throws IOException {
            return GsonUtils.fromJson(text(), type);
        }

        /**
         * Deserializes json response
         *
         * @param type Class to deserialize into
         * @return Response Object
         */
        public <T> T json(Class<T> type) throws IOException {
            return GsonUtils.fromJson(text(), type);
        }

        /**
         * Get the raw response stream of this connection
         *
         * @return InputStream
         */
        public InputStream stream() throws IOException {
            assertOk();
            return req.conn.getInputStream();
        }

        /**
         * Pipe response into OutputStream. Remember to close the OutputStream
         *
         * @param os The OutputStream to pipe into
         */
        public void pipe(OutputStream os) throws IOException {
            try (InputStream is = stream()) {
                IOUtils.pipe(is, os);
            }
        }

        /**
         * Saves the received data to the specified {@link File}
         *
         * @param file The file to save the data to
         * @throws IOException If an I/O error occurred: No such file, file is directory, etc
         */
        public void saveToFile(@NonNull File file) throws IOException {
            saveToFile(file, null);
        }

        /**
         * Saves the received data to the specified {@link File}
         * and verifies its integrity using the specified sha1sum
         *
         * @param file    The file to save the data to
         * @param sha1sum checksum to check the file's integrity. May be null to skip integrity check
         * @throws IOException If an I/O error occurred: No such file, file is directory, integrity check failed, etc
         */
        public void saveToFile(@NonNull File file, @Nullable String sha1sum) throws IOException {
            if (file.exists()) {
                if (!file.canWrite())
                    throw new IOException("Cannot write to file: " + file.getAbsolutePath());
                if (file.isDirectory())
                    throw new IOException("Path already exists and is directory: " + file.getAbsolutePath());
            }

            var parent = file.getParentFile();
            if (parent == null)
                throw new IOException("Only absolute paths are supported.");

            var tempFile = File.createTempFile("download", null, parent);
            try {
                boolean shouldVerify = sha1sum != null;
                var md = shouldVerify ? MessageDigest.getInstance("SHA-1") : null;
                try (
                    var is = shouldVerify ? new DigestInputStream(stream(), md) : stream();
                    var os = new FileOutputStream(tempFile)
                ) {
                    IOUtils.pipe(is, os);

                    if (shouldVerify) {
                        var hash = String.format("%040x", new BigInteger(1, md.digest()));
                        if (!hash.equalsIgnoreCase(sha1sum))
                            throw new IOException(String.format("Integrity check failed. Expected %s, received %s.", sha1sum, hash));
                    }

                    if (!tempFile.renameTo(file))
                        throw new IOException("Failed to rename temp file.");
                } catch (IOException ex) {
                    if (tempFile.exists() && !tempFile.delete())
                        Main.logger.warn("[HTTP#saveToFile] Failed to clean up temp file " + tempFile.getAbsolutePath());
                    throw ex;
                }
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException("Failed to retrieve SHA-1 MessageDigest instance", ex);
            }
        }

        /**
         * Closes the {@link Request} associated with this {@link Response}
         */
        @Override
        public void close() {
            req.close();
        }
    }

    /**
     * Send a simple GET request
     *
     * @param url The url to fetch
     * @return Raw response (String). If you want Json, use simpleJsonGet
     */
    public static String simpleGet(String url) throws IOException {
        return new Request(url, "GET").execute().text();
    }

    /**
     * Download content from the specified url to the specified {@link File}
     *
     * @param url        The url to download content from
     * @param outputFile The file to save to
     */
    public static void simpleDownload(String url, File outputFile) throws IOException {
        new Request(url).execute().saveToFile(outputFile);
    }

    /**
     * Send a simple GET request
     *
     * @param url    The url to fetch
     * @param schema Class to <a href="https://en.wikipedia.org/wiki/Serialization">deserialize</a> the response into
     * @return Response Object
     */
    public static <T> T simpleJsonGet(String url, Type schema) throws IOException {
        String res = simpleGet(url);
        return GsonUtils.fromJson(res, schema);
    }

    /**
     * Send a simple GET request
     *
     * @param url    The url to fetch
     * @param schema Class to <a href="https://en.wikipedia.org/wiki/Serialization">deserialize</a> the response into
     * @return Response Object
     */
    public static <T> T simpleJsonGet(String url, Class<T> schema) throws IOException {
        return simpleJsonGet(url, (Type) schema);
    }

    /**
     * Send a simple POST request
     *
     * @param url  The url to fetch
     * @param body The request body
     * @return Raw response (String). If you want Json, use simpleJsonPost
     */
    public static String simplePost(String url, String body) throws IOException {
        return new Request(url, "POST").executeWithBody(body).text();
    }

    /**
     * Send a simple POST request and parse the JSON response
     *
     * @param url    The url to fetch
     * @param body   The request body
     * @param schema Class to <a href="https://en.wikipedia.org/wiki/Serialization">deserialize</a> the response into
     * @return Response deserialized into the provided Class
     */
    public static <T> T simpleJsonPost(String url, String body, Type schema) throws IOException {
        String res = simplePost(url, body);
        return GsonUtils.fromJson(res, schema);
    }

    // This is just here for proper Generics so you can do simpleJsonPost(url, body, myClass).myMethod() without having to cast

    /**
     * Send a simple POST request and parse the JSON response
     *
     * @param url    The url to fetch
     * @param body   The request body
     * @param schema Class to <a href="https://en.wikipedia.org/wiki/Serialization">deserialize</a> the response into
     * @return Response deserialized into the provided Class
     */
    public static <T> T simpleJsonPost(String url, String body, Class<T> schema) throws IOException {
        return simpleJsonPost(url, body, (Type) schema);
    }

    /**
     * Send a simple POST request with JSON body
     *
     * @param url    The url to fetch
     * @param body   The request body
     * @param schema Class to <a href="https://en.wikipedia.org/wiki/Serialization">deserialize</a> the response into
     * @return Response deserialized into the provided Class
     */
    public static <T> T simpleJsonPost(String url, Object body, Type schema) throws IOException {
        return new Request(url).executeWithJson(body).json(schema);
    }

    // This is just here for proper Generics so you can do simpleJsonPost(url, body, myClass).myMethod() without having to cast

    /**
     * Send a simple POST request with JSON body
     *
     * @param url    The url to fetch
     * @param body   The request body
     * @param schema Class to <a href="https://en.wikipedia.org/wiki/Serialization">deserialize</a> the response into
     * @return Response deserialized into the provided Class
     */
    public static <T> T simpleJsonPost(String url, Object body, Class<T> schema) throws IOException {
        return simpleJsonPost(url, body, (Type) schema);
    }
}
