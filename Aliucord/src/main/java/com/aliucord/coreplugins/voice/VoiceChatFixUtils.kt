package com.aliucord.coreplugins.voice

private const val GROUP_SIZE = 5
private const val DESIRED_LEN = 30
private const val GROUP_MODULUS = 100000UL // 10.0.pow(groupSize).toULong()

internal fun formatFingerprint(b64: String): String {
    if (b64.isEmpty()) return ""

    val data = b64.decodeBase64ToArray() ?: return ""

    if (data.size < DESIRED_LEN) return ""

    val sb = StringBuilder(DESIRED_LEN + DESIRED_LEN / GROUP_SIZE)

    for (group in 0 until DESIRED_LEN / GROUP_SIZE) {
        val start = group * GROUP_SIZE
        var value = 0UL

        for (index in 0 until GROUP_SIZE) {
            value = (value shl 8) or data[start + index].toUByte().toULong()
        }

        if (group > 0) sb.append(' ')

        sb.append((value % GROUP_MODULUS).toString().padStart(GROUP_SIZE, '0'))
    }

    return sb.toString()
}
