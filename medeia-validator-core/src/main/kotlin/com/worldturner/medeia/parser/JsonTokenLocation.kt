package com.worldturner.medeia.parser

import com.worldturner.medeia.pointer.JsonPointer

interface JsonTokenLocation {
    val pointer: JsonPointer
    /** Nesting level in JSON data at which the error occurred. */
    val level: Int
    /** Optional name of the InputSource. */
    val inputSourceName: String?
    val line: Int get() = -1
    val column: Int get() = -1
    /**
     * Property names seen so far in the current JSON object.
     * Only reliable when current token type is {@link JsonTokenType#END_OBJECT}.
     */
    val propertyNames: Set<String>
}
