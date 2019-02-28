package com.worldturner.medeia.parser

import java.io.Closeable

interface JsonParserAdapter : Closeable {
    fun parseAll()
}