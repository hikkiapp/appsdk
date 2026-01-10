package hikki.sdk.utils

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays


object UniversalMorse {
    private val ENC_BITS = IntArray(16)
    private val ENC_LEN = IntArray(16)
    private val DEC_MAP = IntArray(1024) { -1 }

    private val CODES = intArrayOf(
        0x1, 0x2, 0x11, 0x12, 0x21, 0x22, 0x111, 0x112,
        0x121, 0x122, 0x211, 0x212, 0x221, 0x222, 0x1111, 0x1112
    )

    init {
        for (i in CODES.indices) {
            val raw = Integer.toHexString(CODES[i])
            var bits = 0
            var len = 0
            var signature = 0

            for (idx in raw.indices) {
                val type = raw[idx]
                if (idx > 0) {
                    bits = bits shl 1
                    len++
                }
                if (type == '1') {
                    bits = (bits shl 1) or 1
                    len++
                    signature = (signature shl 2) or 1
                } else {
                    bits = (bits shl 3) or 7
                    len += 3
                    signature = (signature shl 2) or 2
                }
            }
            ENC_BITS[i] = bits
            ENC_LEN[i] = len
            if (signature < DEC_MAP.size) {
                DEC_MAP[signature] = i
            }
        }
    }

    fun encode(plaintext: String): ByteArray {
        val input = plaintext.toByteArray(Charsets.UTF_8)
        var output = ByteArray(8 + (input.size * 3).coerceAtLeast(16))
        var byteIdx = 8

        var buffer = 0L
        var bufLen = 0
        var totalBits = 0L

        for (i in input.indices) {
            val b = input[i].toInt()

            val hIdx = (b shr 4) and 0x0F
            buffer = (buffer shl ENC_LEN[hIdx]) or (ENC_BITS[hIdx].toLong() and 0xFFFFFFFFL)
            bufLen += ENC_LEN[hIdx]

            buffer = buffer shl 3
            bufLen += 3

            val lIdx = b and 0x0F
            buffer = (buffer shl ENC_LEN[lIdx]) or (ENC_BITS[lIdx].toLong() and 0xFFFFFFFFL)
            bufLen += ENC_LEN[lIdx]

            if (i < input.size - 1) {
                buffer = buffer shl 7
                bufLen += 7
            }

            while (bufLen >= 8) {
                if (byteIdx >= output.size) output = Arrays.copyOf(output, output.size * 2)
                bufLen -= 8
                output[byteIdx++] = ((buffer shr bufLen) and 0xFF).toByte()
            }
            totalBits = totalBits + ENC_LEN[hIdx] + 3 + ENC_LEN[lIdx] + if (i < input.size - 1) 7 else 0
        }

        if (bufLen > 0) {
            if (byteIdx >= output.size) output = Arrays.copyOf(output, output.size + 1)
            output[byteIdx++] = ((buffer shl (8 - bufLen)) and 0xFF).toByte()
        }

        var t = totalBits
        for (j in 0..7) {
            output[j] = (t and 0xFF).toByte()
            t = t shr 8
        }

        return if (byteIdx == output.size) output else Arrays.copyOf(output, byteIdx)
    }

    fun decode(data: ByteArray): String {
        if (data.size < 8) return ""

        var totalBits = 0L
        for (j in 0..7) {
            totalBits = totalBits or ((data[j].toLong() and 0xFF) shl (j * 8))
        }

        val outCap = (totalBits / 10).toInt().coerceAtLeast(16)
        var output = ByteArray(outCap)
        var outIdx = 0

        var currentByte = 0
        var highNibble = true
        var sig = 0

        var ones = 0
        var zeros = 0
        var readBits = 0L
        var dataIdx = 8
        var bitPos = 7

        fun commitNibble() {
            if (sig != 0) {
                val nibble = DEC_MAP[sig]
                if (nibble != -1) {
                    if (highNibble) {
                        currentByte = nibble shl 4
                        highNibble = false
                    } else {
                        currentByte = currentByte or nibble
                        if (outIdx >= output.size) output = Arrays.copyOf(output, output.size * 2)
                        output[outIdx++] = currentByte.toByte()
                        highNibble = true
                        currentByte = 0
                    }
                }
                sig = 0
            }
        }

        while (readBits < totalBits) {
            if (dataIdx >= data.size) break
            val bit = (data[dataIdx].toInt() shr bitPos) and 1

            bitPos--
            if (bitPos < 0) {
                bitPos = 7
                dataIdx++
            }
            readBits++

            if (bit == 1) {
                if (zeros > 0) {
                    if (zeros >= 3) commitNibble()
                    zeros = 0
                }
                ones++
            } else {
                if (ones > 0) {
                    val type = if (ones >= 3) 2 else 1
                    sig = (sig shl 2) or type
                    ones = 0
                }
                zeros++
            }
        }

        if (ones > 0) {
            val type = if (ones >= 3) 2 else 1
            sig = (sig shl 2) or type
        }
        commitNibble()

        return String(output, 0, outIdx, Charsets.UTF_8)
    }

    fun encodeToBase64(plaintext: String): String {
        return Base64.encodeToString(encode(plaintext), Base64.NO_WRAP)
    }

    fun decodeFromBase64(base64String: String): String {
        if (base64String.isEmpty()) return ""
        return try {
            decode(Base64.decode(base64String, Base64.DEFAULT))
        } catch (e: Exception) {
            ""
        }
    }
}