package com.aliucord

import com.aliucord.Http.simpleJsonGet
import com.aliucord.Http.simpleJsonPost
import com.aliucord.utils.GsonUtils.fromJson
import com.aliucord.utils.GsonUtils.gson
import com.aliucord.utils.GsonUtils.toJson
import com.aliucord.utils.IOUtils
import com.aliucord.utils.RNSuperProperties
import com.aliucord.utils.RNSuperProperties.superPropertiesBase64
import com.discord.utilities.analytics.AnalyticSuperProperties
import com.discord.utilities.rest.RestAPI.AppHeadersProvider
import com.google.gson.Gson
import java.io.*
import java.lang.reflect.Type
import java.math.BigInteger
import java.net.*
import java.nio.charset.StandardCharsets
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.*
import java.util.zip.CRC32

/**
 * HTTP(s) utilities for sending and parsing various types of requests and response, including:
 * - Proper v126.21 style requests
 * - Proper RNA style requests
 * - Downloading files with checksums
 * - Multipart requests
 */
@Suppress("unused")
object Http {
    /**
     * Send a simple GET request
     *
     * @param url The url to fetch
     * @return The raw text response. If you want decoded JSON, then use [simpleJsonGet].
     */
    @JvmStatic
    @Throws(IOException::class)
    fun simpleGet(url: String): String {
        return Request(url, "GET").execute().text()
    }

    /**
     * Download content from the specified url to the specified output file.
     *
     * @param url        The url to download content from
     * @param outputFile The file to save to
     */
    @JvmStatic
    @Throws(IOException::class)
    fun simpleDownload(url: String, outputFile: File) {
        Request(url).execute().saveFile(outputFile)
    }

    /**
     * Send a simple GET request and decode the response as a specific type.
     *
     * @param url    The remote url to fetch
     * @param schema Class to deserialize the response into.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun <T> simpleJsonGet(url: String, schema: Type?): T? { // TODO: check whether schema can be null for all of these
        return gson.fromJson(simpleGet(url), schema)
    }

    /**
     * Send a simple GET request
     *
     * @param url    The url to fetch
     * @param schema Class to deserialize the response into.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun <T> simpleJsonGet(url: String, schema: Class<T>): T? {
        return simpleJsonGet(url, schema)
    }

    /**
     * Send a simple POST request
     *
     * @param url  The remote url to fetch
     * @param body The request body as text.
     * @return The raw text response. If you want decoded JSON, then use [simpleJsonPost].
     */
    @JvmStatic
    @Throws(IOException::class)
    fun simplePost(url: String, body: String): String {
        return Request(url, "POST").executeWithBody(body).text()
    }

    /**
     * Send a simple POST request with JSON body
     *
     * @param url    The remote url to fetch
     * @param body   The remote request body to serialize into JSON.
     * @param schema Class to deserialize the response into.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun <T> simpleJsonPost(url: String, body: Any?, schema: Type?): T? {
        return Request(url).executeWithJson(body).json(schema)
    }

    /**
     * Send a simple POST request with JSON body
     *
     * @param url    The remote url to fetch
     * @param body   The remote request body to serialize into JSON.
     * @param schema Class to deserialize the response into.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun <T> simpleJsonPost(url: String, body: Any?, schema: Class<T>): T? {
        return simpleJsonPost(url, body, schema as Type)
    }

    /**
     * Performs a GET request to a Discord route
     *
     * @param builder A QueryBuilder for this request
     * @throws IOException If an I/O exception occurs
     */
    @JvmStatic
    @Throws(IOException::class)
    fun newDiscordRequest(builder: QueryBuilder): Request =
        newDiscordRequest(builder.toString(), "GET")

    /**
     * Performs a request to a Discord route
     *
     * @param route A Discord route, such as `/users/@me`
     * @param method [HTTP method](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods). By default, this is `GET`
     * @throws IOException If an I/O exception occurs
     */
    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun newDiscordRequest(route: String, method: String = "GET"): Request {
        return Request(getDiscordRoute(route), method)
            .setHeader("User-Agent", AppHeadersProvider.INSTANCE.userAgent)
            .setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.superPropertiesStringBase64)
            .setHeader("Accept", "*/*")
            .setHeader("Authorization", AppHeadersProvider.INSTANCE.authToken)
            .setHeader("Accept-Language", AppHeadersProvider.INSTANCE.acceptLanguages)
            .setHeader("X-Discord-Locale", AppHeadersProvider.INSTANCE.locale)
    }

    /**
     * Performs a GET request to a Discord route using RN headers
     *
     * @param builder QueryBuilder
     * @throws IOException If an I/O exception occurs
     */
    @JvmStatic
    @Throws(IOException::class)
    fun newDiscordRNRequest(builder: QueryBuilder): Request =
        newDiscordRNRequest(builder.toString(), "GET")

    /**
     * Performs a request to a Discord route using RN headers
     *
     * @param route  A Discord route, such as `/users/@me`
     * @param method [HTTP method](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods). By default, this is `GET`
     * @throws IOException If an I/O exception occurs
     */
    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun newDiscordRNRequest(route: String, method: String = "GET"): Request {
        return Request(getDiscordRoute(route), method)
            .setHeader("User-Agent", RNSuperProperties.userAgent)
            .setHeader("X-Super-Properties", superPropertiesBase64)
            .setHeader("Accept-Language", AppHeadersProvider.INSTANCE.acceptLanguages)
            .setHeader("Accept", "*/*")
            .setHeader("Authorization", AppHeadersProvider.INSTANCE.authToken)
            .setHeader("X-Discord-Locale", AppHeadersProvider.INSTANCE.locale)
            .setHeader("X-Discord-Timezone", TimeZone.getDefault().id)
    }

    @JvmStatic
    private fun getDiscordRoute(route: String): String {
        return if (route.startsWith("http")) {
            route.replaceFirst("http://", "https://")
        } else {
            "https://discord.com/api/v9$route"
        }
    }

    /** Request Builder  */
    class Request
    /**
     * Builds a request with the specified url and method.
     * This does not immediately start the request.
     *
     * @param url    Remote url
     * @param method [HTTP method](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods)
     * @throws IOException If an I/O exception occurs
     */
    @Throws(IOException::class)
    @JvmOverloads
    constructor(
        url: String,
        method: String = "GET",
    ) : Closeable {
        /**
         * Builds a `GET` request with the specified QueryBuilder.
         * This does not immediately start the request.
         *
         * @param builder A [QueryBuilder]
         * @throws IOException If an I/O exception occurs
         */
        constructor(builder: QueryBuilder) : this(builder.toString(), "GET")

        /** The connection of this Request. */
        @JvmField
        val conn: HttpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
            setRequestMethod(method.uppercase(Locale.getDefault()))
            addRequestProperty("User-Agent", USER_AGENT)
        }

        /**
         * Add a header to the request.
         *
         * @param key   the name
         * @param value the value
         * @return self
         */
        fun setHeader(key: String, value: String?): Request {
            conn.setRequestProperty(key, value)
            return this
        }

        /**
         * Sets the request connection and read timeout
         *
         * @param timeout the timeout, in milliseconds
         * @return self
         */
        fun setRequestTimeout(timeout: Int): Request {
            conn.setConnectTimeout(timeout)
            conn.setReadTimeout(timeout)
            return this
        }

        /**
         * Sets whether redirects should be followed
         *
         * @param follow Whether redirects should be followed
         * @return self
         */
        fun setFollowRedirects(follow: Boolean): Request {
            conn.setInstanceFollowRedirects(follow)
            return this
        }

        /**
         * Initiates and executes the request.
         */
        @Throws(IOException::class)
        fun execute(): Response = Response(this)

        /**
         * Executes the request with the specified body.
         * May not be used in GET requests.
         *
         * @param body The request body
         */
        @Throws(IOException::class)
        fun executeWithBody(body: String): Response {
            return executeWithBody(bytes = body.toByteArray())
        }

        /**
         * Executes the request with the specified bytes as the body.
         * May not be used in GET requests.
         *
         * @param bytes The request body in raw bytes
         */
        @Throws(IOException::class)
        fun executeWithBody(bytes: ByteArray): Response {
            if (conn.requestMethod == "GET") {
                throw IOException("Body may not be specified in GET requests")
            }

            setHeader("Content-Length", bytes.size.toString())
            conn.setDoOutput(true)
            conn.getOutputStream().use { out ->
                out.write(bytes, 0, bytes.size)
                out.flush()
            }
            return execute()
        }

        /**
         * Executes the request with the specified object as the JSON body.
         * This may not be used with GET requests.
         *
         * @param body The request body
         */
        @Throws(IOException::class)
        fun executeWithJson(body: Any?): Response {
            return executeWithJson(gson, body)
        }

        /**
         * Executes the request with the specified object as the JSON body.
         * This may not be used with GET requests.
         *
         * @param gson Gson instance
         * @param body The request body
         */
        @Throws(IOException::class)
        fun executeWithJson(gson: Gson, body: Any?): Response {
            setHeader("Content-Type", "application/json")

            return executeWithBody(gson.toJson(body))
        }

        /**
         * Executes the request with the specified object as
         * [URL encoded form data](https://url.spec.whatwg.org/#application/x-www-form-urlencoded).
         * This may not be used with GET requests.
         *
         * @param params the form data
         * @throws IOException if an I/O exception occurred
         */
        @Throws(IOException::class)
        fun executeWithUrlEncodedForm(params: MutableMap<String, Any?>): Response {
            val qb = QueryBuilder("")
            for (entry in params.entries) {
                qb.append(entry.key, Objects.toString(entry.value))
            }

            setHeader("Content-Type", "application/x-www-form-urlencoded")

            return executeWithBody(qb.toString().substring(1))
        }

        /**
         * Execute the request with the specified object as multipart form-data.May not be used in GET requests.
         *
         * Please note that this will set the [Transfer-Encoding](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Transfer-Encoding)
         * as chunked. Some servers may not support this. To upload un-chunked (which can lead to OOM with large files),
         * set [doChunkedUploading] to false.
         *
         * @param params Map of params. The values will be converted in the following way:
         *  - [File]: Appends filename and content-type, then append the file's contents
         *  - [InputStream]: Read the stream fully and append the bytes
         *  - [Any]: Object/null is stringified and appended as text.
         * @param doChunkedUploading Whether to upload in chunks. If this is false, a buffer will be allocated to hold the entire
         * multipart form. When uploading large files this way, you will run out of memory. Not every server
         * supports this, and this also does not support redirects.
         * @throws IOException if an I/O exception occurred.
         */
        @JvmOverloads
        @Throws(IOException::class)
        fun executeWithMultipartForm(params: MutableMap<String, Any?>, doChunkedUploading: Boolean = true): Response {
            val boundary = "--${UUID.randomUUID()}--"

            if (conn.requestMethod == "GET") {
                throw IOException("MultiPartForm may not be specified in GET requests")
            }

            setHeader("Content-Type", "multipart/form-data; boundary=$boundary")
            if (doChunkedUploading) {
                conn.setChunkedStreamingMode(-1)
            }
            conn.setDoOutput(true)

            MultiPartBuilder(boundary, conn.getOutputStream()).use { builder ->
                for (entry in params.entries) {
                    when (val value = entry.value) {
                        is File -> builder.appendFile(entry.key, value)
                        is InputStream -> builder.appendStream(entry.key, value)
                        else -> builder.appendField(entry.key, Objects.toString(entry.value))
                    }
                }
                builder.finish()
                return execute()
            }
        }

        /** Closes and/or kills this request  */
        override fun close() {
            conn.disconnect()
        }

        private companion object {
            val USER_AGENT = String.format("Aliucord/%s (https://github.com/Aliucord/Aliucord)", BuildConfig.VERSION)
        }
    }

    /** A pending HTTP response obtained by calling [Request.execute] */
    class Response
    /**
     * Construct a Response
     *
     * @param req The http request to execute
     * @throws IOException If an error occurred connecting to the server
     */
    @Throws(IOException::class)
    constructor(
        private val req: Request,
    ) : Closeable {
        /** The [status code](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) of this response  */
        val statusCode: Int = req.conn.getResponseCode()

        /** The [status message](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) of this response  */
        val statusMessage: String? = req.conn.getResponseMessage()

        /** Whether the request was successful (status code 2xx)  */
        @Suppress("ConvertTwoComparisonsToRangeCheck") // TODO: IntRange fix?
        fun ok(): Boolean = statusCode >= 200 && statusCode < 300

        /** Throws an HttpException if this request was unsuccessful  */
        @Throws(HttpException::class)
        fun assertOk() {
            if (!ok()) throw HttpException(req, this)
        }

        /** Get the response body as a [String]  */
        @Throws(IOException::class)
        fun text(): String = stream().use { IOUtils.readAsText(it) }

        /** Get the response body as a byte array */
        @Throws(IOException::class)
        fun getBytes(): ByteArray = stream().use { IOUtils.readBytes(it) }

        /**
         * Deserializes the response as JSON
         *
         * @param type Type to deserialize into
         */
        @Throws(IOException::class)
        fun <T> json(type: Type?): T? {
            return json(gson, type)
        }

        /**
         * Deserializes the response as JSON
         *
         * @param gson Gson instance
         * @param type Type to deserialize into
         */
        @Throws(IOException::class)
        fun <T> json(gson: Gson, type: Type?): T? {
            return gson.fromJson(text(), type)
        }

        /**
         * Deserializes the response as JSON
         *
         * @param type Class to deserialize into
         * @return Response Object
         */
        @Throws(IOException::class)
        fun <T> json(type: Class<T?>): T? {
            return json<T?>(gson, type)
        }

        /**
         * Deserializes the response as JSON
         *
         * @param gson Gson instance
         * @param type Class to deserialize into
         * @return Response Object
         */
        @Throws(IOException::class)
        fun <T> json(gson: Gson, type: Class<T?>): T? {
            return gson.fromJson(text(), type)
        }

        /**
         * Get the raw response stream of this connection
         *
         * @return InputStream
         */
        @Throws(IOException::class)
        fun stream(): InputStream {
            assertOk()
            return req.conn.getInputStream()
        }

        /**
         * Pipe response into OutputStream. Remember to close the OutputStream
         *
         * @param os The OutputStream to pipe into
         */
        @Throws(IOException::class)
        fun pipe(os: OutputStream) {
            stream().use { IOUtils.pipe(it, os) }
        }

        /**
         * Downloads, verifies, and saves the received data to the specified [File].
         *
         * @param outFile The file to save the data to. A separate, temporary, file is used while downloading.
         * @param verify  A verification callback to check the file before it is saved to [outFile].
         * @throws IOException If an I/O error occurred: No such file, file is directory, integrity check failed, etc
         */
        @Throws(IOException::class)
        private fun saveFile(stream: InputStream, outFile: File, verify: (() -> Unit)? = null) {
            if (outFile.parentFile == null)
                throw IOException("Cannot write to non-absolute paths: ${outFile.path}")
            if (outFile.exists()) {
                if (outFile.isDirectory())
                    throw IOException("Path already exists and is directory: ${outFile.absolutePath}")
                if (!outFile.canWrite())
                    throw IOException("Cannot write to path: ${outFile.absolutePath}")
            }

            val tmpFile = File.createTempFile(
                /* prefix = */ "download",
                /* suffix = */ "tmp",
                /* directory = */ outFile.parentFile,
            )
            stream.use { stream ->
                tmpFile.outputStream().use { out ->
                    IOUtils.pipe(stream, out)
                }
            }

            try {
                verify?.invoke()

                if (!tmpFile.renameTo(outFile)) {
                    throw IOException("Failed to save to target path: ${outFile.absolutePath}")
                }
            } finally {
                tmpFile.delete()
            }
        }

        /**
         * Downloads the received data to the specified [File].
         *
         * @param outFile The file to save the data to. A separate, temporary, file is used while downloading.
         * @throws IOException If an I/O error occurred: No such file, file is directory, integrity check failed, etc
         */
        fun saveFile(outFile: File) {
            saveFile(stream(), outFile)
        }

        /**
         * Saves the received data to the specified [File],
         * and verifies its integrity using the specified sha1 hash.
         *
         * @param outFile    The file to save the data to
         * @param sha1sum Checksum to check the file's integrity.
         * @throws IOException If an I/O error occurred: No such file, file is directory, integrity check failed, etc
         */
        fun saveFileSHA1(outFile: File, sha1sum: String) {
            val digest = MessageDigest.getInstance("SHA-1")

            saveFile(DigestInputStream(stream(), digest), outFile) {
                val hash = String.format("%040x", BigInteger(1, digest.digest()))
                if (!hash.equals(sha1sum, ignoreCase = true)) {
                    throw IOException("Integrity check failed. Expected $sha1sum, received $hash")
                }
            }
        }

        /**
         * Saves the received data to the specified [File],
         * and verifies its integrity using the specified CRC32 hash.
         *
         * @param outFile    The file to save the data to
         * @param crc32 Checksum to check the file's integrity.
         * @throws IOException If an I/O error occurred: No such file, file is directory, integrity check failed, etc
         */
        fun saveFileCRC32(outFile: File, crc32: String) {
            val digestStream = object : FilterInputStream(stream()) {
                val digest = CRC32()

                override fun read(): Int {
                    val ch = `in`.read()
                    if (ch != -1) {
                        digest.update(ch)
                    }
                    return ch
                }

                override fun read(b: ByteArray?, off: Int, len: Int): Int {
                    val result = `in`.read(b, off, len)
                    if (result != -1) {
                        digest.update(b, off, result)
                    }
                    return result
                }
            }

            saveFile(digestStream, outFile) {
                val hash = String.format("%040x", digestStream.digest.value)
                if (!hash.equals(crc32, ignoreCase = true)) {
                    throw IOException("Integrity check failed. Expected $crc32, received $hash")
                }
            }
        }

        /**
         * Closes the [Request] associated with this [Response].
         */
        override fun close() {
            req.close()
        }
    }

    /** A failed HTTP request. */
    class HttpException
    /** Creates a new HttpException for the specified Request and Response */
    @Throws(IOException::class)
    constructor(
        /** The raw Request object  */
        val req: Request,
        /** The raw Response object  */
        val res: Response,
    ) : IOException() {
        /** The url of this request  */
        val url: URL? = req.conn.getURL()

        /** The HTTP method of this request  */
        val method: String? = req.conn.requestMethod

        /** The status code of the response  */
        val statusCode: Int = res.statusCode

        /** The status message of the response  */
        val statusMessage: String? = res.statusMessage

        /** The response sent by the server */
        val errorResponse: String = try {
            req.conn.errorStream
                .use { eis -> IOUtils.readAsText(eis) }
        } catch (e: IOException) {
            "<failed to read response>"
        }

        override val message: String = buildString {
            append(res.statusCode)
            append(": ")
            append(res.statusMessage)
            append(" (")
            append(req.conn.getURL())
            append(')')
            append('\n')
            append(errorResponse)
        }
    }

    /** Request query string builder  */
    class QueryBuilder(baseUrl: String) {
        private val sb: StringBuilder = StringBuilder("$baseUrl?")

        /**
         * Appends a query parameter. Will automatically be URL encoded for you.
         *
         * @param key   The parameter key
         * @param value The parameter value
         * @return self
         */
        fun append(key: String, value: String): QueryBuilder {
            sb.apply {
                append(URLEncoder.encode(key, "UTF-8"))
                append('=')
                append(URLEncoder.encode(value, "UTF-8"))
                append('&')
            }

            return this
        }

        /**
         * Build the finished Url
         */
        override fun toString(): String {
            return sb.toString()
                .drop(1) // Remove last & or ? if no query specified
        }
    }

    /** Utility to build Multipart requests  */
    class MultiPartBuilder
    /**
     * Construct a new MultiPartBuilder writing to the provided OutputStream
     *
     * @param boundary Boundary
     * @param outputStream       OutputStream to write to. Should optimally be the result of connection.getOutputStream()
     */
    constructor(
        boundary: String,
        private val outputStream: OutputStream
    ) : Closeable {
        private val boundary: ByteArray = boundary.toByteArray(StandardCharsets.UTF_8)

        @Throws(IOException::class)
        private fun append(s: String): MultiPartBuilder {
            return append(s.toByteArray(StandardCharsets.UTF_8))
        }

        @Throws(IOException::class)
        private fun append(b: ByteArray): MultiPartBuilder {
            outputStream.write(b)
            return this
        }

        /**
         * Appends an entire file to the request. This does not perform any streaming.
         *
         * @param fieldName  The parameter field name
         * @param uploadFile The parameter file
         * @return self
         */
        @Throws(IOException::class)
        fun appendFile(fieldName: String, uploadFile: File): MultiPartBuilder {
            append(PREFIX).append(boundary).append(LINE_FEED)
            append("Content-Disposition: form-data; name=\"")
                .append(fieldName)
                .append("\"; filename=\"")
                .append(uploadFile.getName())
                .append("\"")
                .append(LINE_FEED)
            append("Content-Type: ")
                .append(URLConnection.guessContentTypeFromName(uploadFile.getName()))
                .append(LINE_FEED)
            append("Content-Transfer-Encoding: binary").append(LINE_FEED)
            append(LINE_FEED)
            outputStream.flush()

            FileInputStream(uploadFile).use { inputStream ->
                IOUtils.pipe(inputStream, outputStream)
            }
            append(LINE_FEED)
            outputStream.flush()

            return this
        }


        /**
         * Append InputStream. Will automatically be encoded for you
         *
         * @param fieldName The parameter field name
         * @param is        The parameter stream
         * @return self
         */
        @Throws(IOException::class)
        fun appendStream(fieldName: String, `is`: InputStream): MultiPartBuilder {
            append(PREFIX).append(boundary).append(LINE_FEED)
            append("Content-Disposition: form-data; name=\"")
                .append(fieldName)
                .append("\"")
                .append(LINE_FEED)
            append("Content-Transfer-Encoding: binary").append(LINE_FEED)
            append(LINE_FEED)
            outputStream.flush()

            IOUtils.pipe(`is`, outputStream)

            append(LINE_FEED)
            outputStream.flush()

            return this
        }

        /**
         * Append field. Will automatically be encoded for you
         *
         * @param fieldName The parameter field name
         * @param value     The parameter value
         * @return self
         */
        @Throws(IOException::class)
        fun appendField(fieldName: String, value: String): MultiPartBuilder {
            append(PREFIX).append(boundary).append(LINE_FEED)
            append("Content-Disposition: form-data; name=\"")
                .append(fieldName)
                .append("\"")
                .append(LINE_FEED)
            append(LINE_FEED)
            append(value).append(LINE_FEED)
            outputStream.flush()
            return this
        }

        /**
         * Finishes this MultiPartForm. This should be called last.
         * Calling any other methods on this Builder after calling this will lead to undefined behavior.
         */
        @Throws(IOException::class)
        fun finish() {
            append(PREFIX).append(boundary).append(PREFIX).append(LINE_FEED)
            outputStream.flush()
            close()
        }

        @Throws(IOException::class)
        override fun close() {
            outputStream.close()
        }

        private companion object {
            const val LINE_FEED = "\r\n"
            const val PREFIX = "--"
        }
    }
}
