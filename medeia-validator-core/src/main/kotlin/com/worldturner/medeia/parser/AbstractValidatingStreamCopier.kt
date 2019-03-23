package com.worldturner.medeia.parser

import com.worldturner.medeia.schema.validation.SchemaValidator
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

open class AbstractValidatingStreamCopier(
    protected val source: InputStream,
    protected val target: OutputStream,
    protected val validator: SchemaValidator,
    protected val inputSourceName: String?
) : Closeable {

    protected inner class ValidatorInputStream : InputStream() {
        override fun read(): Int {
            val r = source.read()
            if (r >= 0)
                target.write(r)
            return r
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val r = source.read(b, off, len)
            if (r > 0)
                target.write(b, off, r)
            return r
        }
    }

    override fun close() {
        source.close()
    }
}