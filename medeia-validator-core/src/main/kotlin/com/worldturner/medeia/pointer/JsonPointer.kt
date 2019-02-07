package com.worldturner.medeia.pointer

data class JsonPointer(val text: String) {
    override fun toString(): String = text

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
