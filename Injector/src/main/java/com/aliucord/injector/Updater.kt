package com.aliucord.injector

import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private const val DATA_URL = "https://builds.aliucord.com/data.json"
private const val CORE_URL = "https://builds.aliucord.com/Aliucord.zip"
private const val USER_AGENT = "Aliucord Injector/${BuildConfig.VERSION} (https://github.com/Aliucord/Aliucord)"

/**
 * Downloads the latest Aliucord core to [outputFile].
 */
internal fun downloadLatestCore(outputFile: File) {
    val tmpFile = outputFile.resolveSibling(outputFile.name + ".tmp")

    Logger.d("Downloading latest Aliucord core from $CORE_URL...")
    val startTime = System.currentTimeMillis()

    val conn = URL(CORE_URL).openConnection() as HttpURLConnection
    conn.setRequestProperty("User-Agent", USER_AGENT)
    conn.useCaches = false

    conn.getInputStream().use { stream ->
        tmpFile.outputStream().use { out ->
            val buffer = ByteArray(1024 * 64) // 64 KiB
            val length = conn.contentLengthLong

            var oldProgress = 0f
            var downloaded = 0
            var bytes = stream.read(buffer)
            while (bytes >= 0) {
                out.write(buffer, 0, bytes)

                val newProgress = (downloaded + bytes) / length.toFloat()
                if (newProgress >= oldProgress + 0.1f) {
                    oldProgress = newProgress
                    Logger.d(String.format(Locale.ROOT,
                        "Downloaded %.2f%% after %sms",
                        newProgress * 100,
                        System.currentTimeMillis() - startTime))
                }

                downloaded += bytes
                bytes = stream.read(buffer)
            }
        }
    }
    tmpFile.renameTo(outputFile)
    Logger.d("Downloaded Aliucord core after ${System.currentTimeMillis() - startTime}ms")
}

/**
 * A parsed model of the data available at [DATA_URL].
 */
internal data class BuildData(
    var discordVersion: Int,
    var kotlinVersion: String,
)

/**
 * Fetches and parses the remote build data that is used to determine if update is possible.
 */
internal fun fetchBuildData(): BuildData {
    Logger.d("Fetching remote build data...")

    val conn = URL(DATA_URL).openConnection() as HttpURLConnection
    conn.setRequestProperty("User-Agent", USER_AGENT)
    conn.useCaches = false

    val data = JSONObject(conn.inputStream.bufferedReader().readText())
    return BuildData(
        discordVersion = data.getInt("versionCode"),
        kotlinVersion = data.optString("kotlinVersion", "1.5.21"),
    )
}
