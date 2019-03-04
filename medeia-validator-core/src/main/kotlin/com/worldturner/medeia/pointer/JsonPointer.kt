package com.worldturner.medeia.pointer

import com.worldturner.util.hasFragment
import java.net.URI

class JsonPointer constructor(val text: String, bypassValidation: Boolean = false) {
    init {
        if (!bypassValidation)
            validate()
    }

    override fun toString(): String = text

    private fun validate() {
        if (text.isEmpty())
            return
        else if (text[0] != '/')
            throw IllegalArgumentException("Needs to start with a / - \"$text\"")
        var index = 0
        while (index < text.length) {
            if (text[index] == '~') {
                if (index + 1 >= text.length) {
                    throw IllegalArgumentException("Invalid ~ at end of pointer - \"$text\"")
                }
                when (text[index + 1]) {
                    '0', '1' -> index++
                    else ->
                        throw IllegalArgumentException(
                            "Invalid ~ followed by ${text[index + 1]} at " +
                                "index ${index + 1} - \"$text\""
                        )
                }
            }
            index++
        }
    }

    fun first() = text.substringBefore('/', text, 1)
    fun firstName(): String =
        first().let { first ->
            if (first.startsWith('/')) decodeJsonPointerElement(first, 1)
            else decodeJsonPointerElement(first, 0)
        }

    fun tail() =
        text.substringFrom('/', "", 1)
            .let { if (it == "") null else JsonPointer(it, bypassValidation = true) }

    fun relativize(childPointer: JsonPointer): JsonPointer {
        val childText = childPointer.toString()
        if (childText.startsWith(text)) {
            return JsonPointer(childText.substring(text.length), bypassValidation = true)
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

internal fun String.substringBefore(
    delimiter: Char,
    missingDelimiterValue: String = this,
    startIndex: Int = 0
): String {
    val index = indexOf(delimiter, startIndex)
    return if (index == -1) missingDelimiterValue else substring(0, index)
}

internal fun String.substringFrom(
    delimiter: Char,
    missingDelimiterValue: String = this,
    startIndex: Int = 0
): String {
    val index = indexOf(delimiter, startIndex)
    return if (index == -1) missingDelimiterValue else substring(index, length)
}

/*
 * Silently ignores invalid escapes - use JsonPointer.validate method to validate.
 */
private fun decodeJsonPointerElement(s: String, offset: Int): String {
    if (s.indexOf('~', offset) == -1) return s.substring(offset)
    val b = StringBuilder(s.length - offset)
    var index = offset
    while (index < s.length) {
        val ch = s[index]
        if (ch == '~') {
            index++
            if (index < s.length) {
                when (s[index]) {
                    '0' -> b.append('~')
                    '1' -> b.append('/')
                }
            }
        } else {
            b.append(ch)
        }
        index++
    }
    return b.toString()
}

private val RJP_SEPARATORS = "#/".toCharArray()

class RelativeJsonPointer(text: String) {
    val levelsUp: Int
    val jsonPointer: JsonPointer?

    init {
        val separatorIndex = text.indexOfAny(RJP_SEPARATORS).let { if (it == -1) text.length else it }
        val levelsUpString = text.substring(0, separatorIndex)
        if (levelsUpString == "0") {
            levelsUp = 0
        } else if (levelsUpString.startsWith("0")) {
            throw NumberFormatException("Invalid leading zero for '$levelsUpString' in '$text'")
        } else {
            try {
                levelsUp = levelsUpString.toInt()
            } catch (e: NumberFormatException) {
                throw NumberFormatException("Invalid number for '$levelsUpString' in '$text'")
            }
        }
        if (separatorIndex == text.length || text[separatorIndex] == '/') {
            jsonPointer = JsonPointer(text.substring(separatorIndex))
        } else {
            jsonPointer = null
        }
    }
}

fun URI.hasJsonPointerFragment(): Boolean =
    if (this.hasFragment() && this.fragment.startsWith('/')) {
        try {
            JsonPointer(this.fragment)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    } else {
        false
    }
