package com.worldturner.util

import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

/**
 * A useful component, mainly for setting up tests and performance tests, but perhaps also in its own right.
 * However not part of the public API of medeia.
 */
class RepeatingByteArrayInputStream(sources: List<ByteArraySource>) : InputStream() {
    constructor(source: ByteArray, repeats: Int) : this(listOf(ByteArraySource(source, repeats)))

    private val iterator = sources.iterator()
    private var current: ByteArraySource? = null
    private var position: Int = 0
    private var repeat: Int = 0

    init {
        advanceCurrent()
    }

    private fun checkPosition() {
        current?.let { current ->
            if (position >= current.source.size) {
                repeat++
                if (repeat >= current.repeats) {
                    advanceCurrent()
                } else {
                    position = 0
                }
            }
        }
    }

    private fun advanceCurrent() {
        current = if (iterator.hasNext()) iterator.next() else null
        repeat = 0
        position = 0
    }

    override fun read(): Int {
        checkPosition()
        current?.let { current ->
            return current.source[position++].toInt() and 0xff
        } ?: return -1
    }

    override fun read(destination: ByteArray, destinationOffset: Int, length: Int): Int {
        if (destinationOffset < 0 || length < 0 || length > destination.size - destinationOffset)
            throw IndexOutOfBoundsException()
        checkPosition()
        return current?.let { current ->
            val actualLength = min(length, current.source.size - position)
            if (length <= 0) {
                0
            } else {
                current.source.copyInto(
                    destination,
                    destinationOffset = destinationOffset,
                    startIndex = position,
                    endIndex = position + actualLength
                )
                position += actualLength
                actualLength
            }
        } ?: -1
    }

    override fun skip(n: Long): Long {
        checkPosition()
        return current?.let { current ->
            val limited = min(max(0, n), Int.MAX_VALUE.toLong())
            val skipLength = min(limited.toInt(), current.source.size - position)
            position += skipLength
            skipLength.toLong()
        } ?: -1
    }

    override fun available(): Int = current?.let { current -> current.source.size - position } ?: 0

    override fun close() = Unit
}

data class ByteArraySource(val source: ByteArray, val repeats: Int = 1)
