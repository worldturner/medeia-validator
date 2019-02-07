package com.worldturner.util

import java.io.Writer

class NullWriter : Writer() {
    override fun close() = Unit
    override fun flush() = Unit
    override fun write(cbuf: CharArray?, off: Int, len: Int) = Unit
    override fun write(c: Int) = Unit
    override fun write(cbuf: CharArray?) = Unit
    override fun write(str: String?) = Unit
    override fun write(str: String?, off: Int, len: Int) = Unit
}