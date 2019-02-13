package com.worldturner.medeia.parser

import com.worldturner.medeia.pointer.JsonPointer

interface JsonTokenLocation {
    val pointer: JsonPointer
    val level: Int
    val line: Int get() = -1
    val column: Int get() = -1
    /**
     * Only reliable when current token type is {@link JsonTokenType#END_OBJECT}.
     */
    val propertyNames: Set<String>
}
