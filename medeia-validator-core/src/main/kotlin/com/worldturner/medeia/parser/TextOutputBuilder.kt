package com.worldturner.medeia.parser

import com.worldturner.medeia.parser.JsonTokenType.END_ARRAY
import com.worldturner.medeia.parser.JsonTokenType.END_OBJECT
import com.worldturner.medeia.parser.JsonTokenType.FIELD_NAME
import com.worldturner.medeia.parser.JsonTokenType.START_ARRAY
import com.worldturner.medeia.parser.JsonTokenType.START_OBJECT
import com.worldturner.medeia.parser.JsonTokenType.VALUE_BOOLEAN_FALSE
import com.worldturner.medeia.parser.JsonTokenType.VALUE_BOOLEAN_TRUE
import com.worldturner.medeia.parser.JsonTokenType.VALUE_NULL
import com.worldturner.medeia.parser.JsonTokenType.VALUE_NUMBER
import com.worldturner.medeia.parser.JsonTokenType.VALUE_TEXT
import java.util.ArrayDeque
import java.util.Deque

class TextOutputBuilder : JsonTokenDataBuilder {
    enum class StructureType { ARRAY, OBJECT }

    private val stack: Deque<StructureType> = ArrayDeque()
    private var result = StringBuilder()
    private var outputComma = false

    override fun consume(token: JsonTokenData) {
        if (outputComma && !token.type.lastStructureToken)
            result.append(',')
        when (token.type) {
            FIELD_NAME -> {
                outputString(token.text!!)
                result.append(':')
            }
            VALUE_TEXT -> outputString(token.text!!)
            VALUE_NULL -> outputNull()
            VALUE_BOOLEAN_FALSE, VALUE_BOOLEAN_TRUE ->
                outputBoolean(token.toBoolean())
            VALUE_NUMBER ->
                if (token.hasLongValue()) outputNumber(token.longValue) else
                    token.integer?.let { outputNumber(it) }
                        ?: token.decimal?.let { outputNumber(it) }
            START_ARRAY -> {
                result.append('[')
                stack.push(StructureType.ARRAY)
            }
            END_ARRAY -> {
                result.append(']')
                stack.pop()
            }
            START_OBJECT -> {
                result.append('{')
                stack.push(StructureType.OBJECT)
            }
            END_OBJECT -> {
                result.append('}')
                stack.pop()
            }
            else -> {
            }
        }
        outputComma = !token.type.firstStructureToken && token.type != FIELD_NAME
    }

    fun outputString(s: String) {
        result.append('"')
        s.forEach {
            when (it) {
                '\n' -> result.append("\\n")
                '\r' -> result.append("\\r")
                '"' -> result.append("\\\"")
                '\\' -> result.append("\\\\")
                '\b' -> result.append("\\b")
                '\u000c' -> result.append("\\f")
                '\t' -> result.append("\\t")
                else -> if (it < ' ')
                    result.append("\\u00${String.format("%02X", it)}")
                else
                    result.append(it)
            }
        }
        result.append('"')
    }

    fun outputNumber(n: Long) {
        result.append(n)
    }

    fun outputNumber(n: Number) {
        result.append(n)
    }

    fun outputBoolean(b: Boolean) {
        result.append(b)
    }

    fun outputNull() {
        result.append("null")
    }

    override fun takeResult(): String =
        result.toString().also { result.clear() }

    override fun toString(): String = takeResult()
}