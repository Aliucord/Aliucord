package com.aliucord.coreplugins.voice

import okio.ByteString

class ByteReader(bytes: ByteArray) {
    private val iterator = bytes.iterator()
    private var remaining = bytes.size

    constructor(bytes: ByteString) : this(bytes.data)

    fun read(): Byte {
        if (remaining == 0) {
            throw IndexOutOfBoundsException("ByteReader reached the end")
        }
        remaining--
        return iterator.next()
    }
    fun read(count: Int): ByteArray {
        if (count > remaining) {
            throw IndexOutOfBoundsException("ByteReader reached the end")
        }
        return Array(remaining) { read() }.toByteArray()
    }
    fun readUint8() = read().toUInt().toInt()
    fun collect() = read(remaining)
    fun collectAsByteString() = ByteString(collect())
}

@Suppress("NOTHING_TO_INLINE")
inline fun ByteString.encodeBase64(): String = f()
inline val ByteString.data: ByteArray get() = i()

@Suppress("NOTHING_TO_INLINE")
inline fun ByteString.prepend(byte: Byte): ByteString = ByteString(arrayOf(byte).toByteArray() + data)

// Method was nuked by dead code removal, so we are copying and bringing it back
// https://github.com/square/okio/blob/50abe8900f2e7bd48d4afc77bda0afd74fc790ac/okio/src/commonMain/kotlin/okio/Base64.kt
// Cheers ^w^
internal fun String.decodeBase64ToArray(): ByteArray? {
    // Ignore trailing '=' padding and whitespace from the input.
    var limit = length
    while (limit > 0) {
        val c = this[limit - 1]
        if (c != '=' && c != '\n' && c != '\r' && c != ' ' && c != '\t') {
            break
        }
        limit--
    }

    // If the input includes whitespace, this output array will be longer than necessary.
    val out = ByteArray((limit * 6L / 8L).toInt())
    var outCount = 0
    var inCount = 0

    var word = 0
    var pos = -1
    while (pos < limit - 1) {
        pos++
        val c = this[pos]

        val bits: Int
        if (c in 'A'..'Z') {
            // char ASCII value
            //  A    65    0
            //  Z    90    25 (ASCII - 65)
            bits = c.code - 65
        } else if (c in 'a'..'z') {
            // char ASCII value
            //  a    97    26
            //  z    122   51 (ASCII - 71)
            bits = c.code - 71
        } else if (c in '0'..'9') {
            // char ASCII value
            //  0    48    52
            //  9    57    61 (ASCII + 4)
            bits = c.code + 4
        } else if (c == '+' || c == '-') {
            bits = 62
        } else if (c == '/' || c == '_') {
            bits = 63
        } else if (c == '\n' || c == '\r' || c == ' ' || c == '\t') {
            continue
        } else {
            return null
        }

        // Append this char's 6 bits to the word.
        word = word shl 6 or bits

        // For every 4 chars of input, we accumulate 24 bits of output. Emit 3 bytes.
        inCount++
        if (inCount % 4 == 0) {
            out[outCount++] = (word shr 16).toByte()
            out[outCount++] = (word shr 8).toByte()
            out[outCount++] = word.toByte()
        }
    }

    val lastWordChars = inCount % 4
    when (lastWordChars) {
        1 -> {
            // We read 1 char followed by "===". But 6 bits is a truncated byte! Fail.
            return null
        }
        2 -> {
            // We read 2 chars followed by "==". Emit 1 byte with 8 of those 12 bits.
            word = word shl 12
            out[outCount++] = (word shr 16).toByte()
        }
        3 -> {
            // We read 3 chars, followed by "=". Emit 2 bytes for 16 of those 18 bits.
            word = word shl 6
            out[outCount++] = (word shr 16).toByte()
            out[outCount++] = (word shr 8).toByte()
        }
    }

    // If we sized our out array perfectly, we're done.
    if (outCount == out.size) return out

    // Copy the decoded bytes to a new, right-sized array.
    return out.copyOf(outCount)
}
