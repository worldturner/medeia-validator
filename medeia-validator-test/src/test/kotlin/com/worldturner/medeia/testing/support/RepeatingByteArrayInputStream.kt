package com.worldturner.medeia.testing.support

import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

class RepeatingByteArrayInputStream(val source: ByteArray, val repeats: Int) : InputStream() {
    private var position: Int = 0
    private var repeat: Int = 0

    override fun read(): Int {
        if (repeat >= repeats)
            return -1
        if (position >= source.size) {
            repeat++
            if (repeat >= repeats)
                return -1
            position = 0
        }
        return source[position++].toInt() and 0xff
    }

    override fun read(destination: ByteArray, destinationOffset: Int, length: Int): Int {
        if (destinationOffset < 0 || length < 0 || length > destination.size - destinationOffset)
            throw IndexOutOfBoundsException()
        if (repeat >= repeats)
            return -1
        if (position >= source.size) {
            repeat++
            if (repeat >= repeats)
                return -1
            position = 0
        }
        val actualLength = min(length, source.size - position)
        return if (length <= 0) {
            0
        } else {
            source.copyInto(
                destination,
                destinationOffset = destinationOffset,
                startIndex = position,
                endIndex = position + actualLength
            )
            position += actualLength
            actualLength
        }
    }

    override fun skip(n: Long): Long {
        if (repeat >= repeats)
            return -1
        if (position >= source.size) {
            if (repeat >= repeats)
                return -1
            repeat++
            position = 0
        }
        val limited = min(max(0, n), Int.MAX_VALUE.toLong())
        val skipLength = min(limited.toInt(), source.size - position)
        position += skipLength
        return skipLength.toLong()
    }

    override fun available(): Int = source.size - position

    override fun close() = Unit
}
