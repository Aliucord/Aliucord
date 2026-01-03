package com.aliucord.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/**
 * Parses a Semantic version in the format of `v1.0.0`.
 * This is always serialized without the `v` prefix.
 */
@JsonAdapter(SemVer.Adapter::class)
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
) : Comparable<SemVer> {
    override fun compareTo(other: SemVer): Int {
        var cmp = 0
        if (0 != major.compareTo(other.major).also { cmp = it })
            return cmp
        if (0 != minor.compareTo(other.minor).also { cmp = it })
            return cmp
        if (0 != patch.compareTo(other.patch).also { cmp = it })
            return cmp

        return 0
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
            if (version == null) return null

            val parts = version.removePrefix("v").split(".")
            if (parts.size != 3)
                return null

            val major = parts[0].toIntOrNull() ?: return null
            val minor = parts[1].toIntOrNull() ?: return null
            val patch = parts[2].toIntOrNull() ?: return null

            return SemVer(major, minor, patch)
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
