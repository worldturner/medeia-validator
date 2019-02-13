package com.worldturner.util

import com.worldturner.util.InternalTokenType.COLON
import com.worldturner.util.InternalTokenType.COMMA
import com.worldturner.util.InternalTokenType.END_ARRAY
import com.worldturner.util.InternalTokenType.END_OBJECT
import com.worldturner.util.InternalTokenType.END_OF_DOCUMENT
import com.worldturner.util.InternalTokenType.FALSE
import com.worldturner.util.InternalTokenType.NULL
import com.worldturner.util.InternalTokenType.NUMBER
import com.worldturner.util.InternalTokenType.START_ARRAY
import com.worldturner.util.InternalTokenType.START_OBJECT
import com.worldturner.util.InternalTokenType.TEXT
import com.worldturner.util.InternalTokenType.TRUE

class JsonValidator(val data: String) {
    var index = -1

    init {
        skipIt()
    }

    private fun throwParseException(): Nothing {
        if (index < data.length)
            throw JsonParseException(data[index], index)
        else
            throw JsonParseException()
    }

    private fun skipIt() {
        val token = nextToken()
        when (token) {
            START_OBJECT -> skipObject()
            START_ARRAY -> skipArray()
            TEXT, NUMBER, TRUE, FALSE, NULL -> {
            }
            else -> throwParseException()
        }
    }

    private fun skipObject() {
        loop@ while (true) {
            skipToken(TEXT)
            skipToken(COLON)
            skipIt()
            val token = nextToken()
            when (token) {
                END_OBJECT -> break@loop
                COMMA -> continue@loop
                else -> throwParseException()
            }
        }
    }

    private fun skipArray() {
        loop@ while (true) {
            skipIt()
            val token = nextToken()
            when (token) {
                END_ARRAY -> break@loop
                COMMA -> continue@loop
                else -> throwParseException()
            }
        }
    }

    private fun nextChar() = if (!hasNextChar()) throwParseException() else data[++index]
    private fun hasNextChar() = index < data.length - 1
    private fun pushbackChar() = index--

    private fun skipWhitespace() {
        while (hasNextChar() && nextChar().isJsonWhitespace()) {
        }
        pushbackChar()
    }

    private fun nextToken(): InternalTokenType {
        skipWhitespace()
        if (!hasNextChar())
            return END_OF_DOCUMENT
        val char = nextChar()
        return when (char) {
            '[' -> START_ARRAY
            ']' -> END_ARRAY
            '{' -> START_OBJECT
            '}' -> END_OBJECT
            ',' -> COMMA
            ':' -> COLON
            '"' -> TEXT.also { skipString() }
            'n' -> NULL.also { skipToken("null") }
            't' -> TRUE.also { skipToken("true") }
            'f' -> FALSE.also { skipToken("false") }
            in '0'..'9', '-', '.' -> NUMBER.also { skipNumber() }
            else -> throwParseException()
        }
    }

    private fun skipString() {
        while (hasNextChar()) {
            val char = nextChar()
            if (char == '"')
                return
            if (char == '\\') {
                val escapedChar = nextChar()
                if (escapedChar == 'u') {
                    skipUnicodeDigits()
                }
            }
        }
        throwParseException()
    }

    private fun skipUnicodeDigits() {
        for (i in 0..4) {
            val char = nextChar()
            when (char) {
                in '0'..'9', in 'A'..'F', in 'a'..'f' -> {
                }
                else -> throwParseException()
            }
        }
    }

    private fun skipToken(expected: InternalTokenType) {
        val token = nextToken()
        if (expected != token) {
            throw JsonParseException("Expected $expected token but seen $token token")
        }
    }

    private fun skipToken(token: String) {
        pushbackChar()
        token.forEach {
            if (!hasNextChar()) {
                throwParseException()
            }
            val char = nextChar()
            if (it != char) {
                throwParseException()
            }
        }
        if (hasNextChar()) {
            val char = nextChar()
            if (char.isLetterOrDigit()) {
                // Textual token should be followed by a non-alphanumeric charcter
                throwParseException()
            } else {
                pushbackChar()
            }
        }
    }

    private fun skipNumber() {
        pushbackChar()
        skipNumberInt()
        skipNumberFractional()
        skipNumberExponent()
        if (hasNextChar()) {
            val char = nextChar()
            if (char.isLetterOrDigit()) {
                // Textual token should be followed by a non-alphanumeric charcter
                throwParseException()
            } else {
                pushbackChar()
            }
        }
    }

    private fun skipNumberExponent() {
        val char = nextChar()
        when (char) {
            'e', 'E' -> {
                skipNumberSign()
                skipDigits()
            }
            else -> pushbackChar()
        }
    }

    private fun skipNumberSign() {
        val char = nextChar()
        when (char) {
            '+', '-' -> {
            }
            else -> pushbackChar()
        }
    }

    private fun skipNumberFractional() {
        val char = nextChar()
        if (char != '.') {
            pushbackChar()
        } else {
            skipDigits()
        }
    }

    private fun skipNumberInt() {
        val char = nextChar()
        when (char) {
            in '1'..'9' -> skipDigits()
            '0' -> {
            }
            '-' -> {
                val nextChar = nextChar()
                when (nextChar) {
                    '0' -> {
                    }
                    in '1'..'9' -> skipDigits()
                }
            }
            else -> throwParseException()
        }
    }

    fun skipDigits() {
        while (true) {
            val char = nextChar()
            if (char !in '0'..'9') {
                pushbackChar()
                break
            }
        }
    }
}

fun Char.isJsonWhitespace(): Boolean =
    when (this) {
        '\t', '\n', '\r', ' ' -> true
        else -> false
    }

enum class InternalTokenType {
    START_OBJECT,
    START_ARRAY,
    END_OBJECT,
    END_ARRAY,
    TEXT,
    NUMBER,
    TRUE,
    FALSE,
    NULL,
    COLON,
    COMMA,
    END_OF_DOCUMENT
}

class JsonParseException(message: String) : RuntimeException(message) {
    constructor(char: Char, index: Int) :
        this("Unexpected character $char at offset $index")

    constructor() :
        this("Incomplete token at end of data")
}
