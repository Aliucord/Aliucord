package com.aliucord.coreplugins.voice

private const val GROUP_SIZE = 5
private const val DESIRED_LEN = 30
private const val GROUP_MODULUS = 100000UL // 10.0.pow(groupSize).toULong()

internal fun formatFingerprint(b64: String): String {
    if (b64.isEmpty()) return ""

    val data = b64.decodeBase64ToArray() ?: return ""

    if (data.size < DESIRED_LEN) return ""

    return (0 until DESIRED_LEN step GROUP_SIZE).joinToString(" ") { start ->
        (start until start + GROUP_SIZE)
            .fold(0UL) { acc, index -> (acc shl 8) or data[index].toUByte().toULong() }
            .rem(GROUP_MODULUS)
            .toString()
            .padStart(GROUP_SIZE, '0')
    }
}
