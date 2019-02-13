package com.worldturner.medeia.pointer

class JsonPointer constructor(val text: String, bypassValidation: Boolean = false) {
    init {
        if (!bypassValidation)
            validate()
    }

    override fun toString(): String = text

    private fun validate() {
        // TODO implement
    }

    fun first() = text.substringBefore('/', text, 1)
    fun tail() =
        text.substringFrom('/', "", 1)
            .let { if (it == "") null else JsonPointer(it) }

    fun relativize(childPointer: JsonPointer): JsonPointer {
        val childText = childPointer.toString()
        if (childText.startsWith(text)) {
            return JsonPointer(childText.substring(text.length))
        } else {
            return childPointer
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as JsonPointer
        return text == other.text
    }

    override fun hashCode(): Int = text.hashCode()
}

fun String.substringBefore(
    delimiter: Char,
    missingDelimiterValue: String = this,
    startIndex: Int = 0
): String {
    val index = indexOf(delimiter, startIndex)
    return if (index == -1) missingDelimiterValue else substring(0, index)
}

fun String.substringFrom(
    delimiter: Char,
    missingDelimiterValue: String = this,
    startIndex: Int = 0
): String {
    val index = indexOf(delimiter, startIndex)
    return if (index == -1) missingDelimiterValue else substring(index, length)
}
