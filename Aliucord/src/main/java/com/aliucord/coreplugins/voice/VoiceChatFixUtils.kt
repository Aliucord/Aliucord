package com.aliucord.coreplugins.voice

import kotlin.math.pow

internal fun formatFingerprint(b64: String): String {
    if (b64.isEmpty()) return ""
    val data = b64.decodeBase64ToArray() ?: return ""
    val groupSize = 5
    val desiredLen = 30
    if (data.size < desiredLen) return ""
    val groupModulus = 10.0.pow(groupSize).toULong()
    var result = ""
    var i = 0
    while (i < desiredLen) {
        var groupValue = 0UL
        var j = groupSize
        while (j >= 1) {
            val n = data[i + groupSize - j].toUByte().toULong()
            groupValue = (groupValue shl 8) or n
            j -= 1
        }
        result += " " + (groupValue % groupModulus).toString().padStart(groupSize, '0')
        i += groupSize
    }
    return result.trimStart()
}
