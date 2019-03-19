package com.worldturner.medeia.parser

import com.worldturner.medeia.api.InputSource
import com.worldturner.medeia.schema.validation.SchemaValidator
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

open class AbstractValidatingStreamCopier(
    source: InputSource,
    protected val target: OutputStream,
    protected val validator: SchemaValidator
) : Closeable {
    private val input = source.stream

    protected inner class ValidatorInputStream : InputStream() {
        override fun read(): Int {
            val r = input.read()
            if (r >= 0)
                target.write(r)
            return r
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val r = input.read(b, off, len)
            if (r > 0)
                target.write(b, off, r)
            return r
        }
    }

    override fun close() {
        input.close()
    }
}