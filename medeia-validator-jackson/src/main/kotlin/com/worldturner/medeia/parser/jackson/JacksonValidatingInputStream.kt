package com.worldturner.medeia.parser.jackson

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import java.io.InputStream
import kotlin.math.min

class JacksonValidatingInputStream(
    private val source: InputStream,
    private val validator: SchemaValidator,
    private val jsonFactory: JsonFactory
) : ByteBufferInputStream() {
    private val parser = createParser()

    private fun createParser(): JsonParser {
        val jsonParser = jsonFactory.createParser(ValidatorInputStream())
        val consumer = SchemaValidatingConsumer(validator)
        return JacksonTokenDataJsonParser(consumer = consumer, jsonParser = jsonParser, inputSourceName = null)
    }

    private inner class ValidatorInputStream : InputStream() {
        override fun read(): Int {
            val r = source.read()
            if (r >= 0)
                buffer(r.toByte())
            return r
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val r = source.read(b, off, len)
            if (r > 0)
                buffer(b, off, r)
            return r
        }
    }

    override fun pullData() {
        parser.nextToken()
    }
}

open class ByteBufferInputStream(initialSize: Int = 8192) : InputStream() {
    private var buffer: ByteArray = ByteArray(initialSize)
    private var writeOffset: Int = 0
    private var readOffset: Int = 0

    fun buffer(byte: Byte) {
        if (writeOffset >= buffer.size) {
            ensureBuffer(1)
        }
        buffer[writeOffset++] = byte
    }

    fun buffer(b: ByteArray, off: Int, len: Int) {
        if (writeOffset + len >= buffer.size) {
            ensureBuffer(len)
        }
        b.copyInto(buffer, destinationOffset = writeOffset, startIndex = off, endIndex = off + len)
        writeOffset += len
    }

    // Compact and/or grow buffer
    private fun ensureBuffer(length: Int) {
        val used = writeOffset - readOffset
        if (buffer.size - used >= length) {
            // Compact
            buffer.copyInto(buffer, destinationOffset = 0, startIndex = readOffset, endIndex = writeOffset)
        } else {
            // Grow
            var newSize = used + length
            val newBuffer = ByteArray(newSize)
            buffer.copyInto(newBuffer, destinationOffset = 0, startIndex = readOffset, endIndex = writeOffset)
            buffer = newBuffer
        }
        readOffset = 0
        writeOffset = used
    }

    override fun read(): Int {
        if (readOffset >= writeOffset)
            pullData()
        return if (readOffset < writeOffset)
            buffer[readOffset++].toInt()
        else
            -1
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (readOffset >= writeOffset)
            pullData()
        return if (readOffset < writeOffset) {
            val available = writeOffset - readOffset
            val readLength = min(len, available)
            buffer.copyInto(b, destinationOffset = off, startIndex = readOffset, endIndex = readOffset + readLength)
            readOffset += readLength
            readLength
        } else {
            -1
        }
    }

    open fun pullData() {}
}