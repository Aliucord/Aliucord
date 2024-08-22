package com.aliucord.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.IllegalArgumentException

/**
 * Parses a Semantic version in the format of `v1.0.0`
 */
@JsonAdapter(SemVer.Adapter::class)
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
) : Comparable<SemVer> {
    override fun compareTo(other: SemVer): Int {
        val pairs = arrayOf(
            major to other.major,
            minor to other.minor,
            patch to other.patch,
        )

        return pairs
            .map { (first, second) -> first.compareTo(second) }
            .find { it != 0 }
            ?: 0
    }

    override fun equals(other: Any?): Boolean {
        val ver = other as? SemVer
            ?: return false

        return ver.major == major &&
            ver.minor == minor &&
            ver.patch == patch
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        return result
    }

    companion object {
        @JvmField
        val Zero = SemVer(0, 0, 0)

        @JvmStatic
        fun parseOrNull(version: String?): SemVer? {
            // Handle 'v' prefix
            val versionString = version?.removePrefix("v")
                ?: return null

            val parts = versionString
                .split(".")
                .mapNotNull { it.toIntOrNull() }
                .takeIf { it.size == 3 }
                ?: return null

            return SemVer(parts[0], parts[1], parts[2])
        }

        @JvmStatic
        fun parse(version: String): SemVer = parseOrNull(version)
            ?: throw IllegalArgumentException("Invalid semver string $version")
    }

    class Adapter : TypeAdapter<SemVer>() {
        override fun read(input: JsonReader): SemVer {
            val string = input.J() // nextString()
            return parse(string)
        }

        override fun write(output: JsonWriter, value: SemVer) {
            val string = value.toString()
            output.H(string) // encode string
        }
    }
}
