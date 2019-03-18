package com.worldturner.medeia.parser.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.util.JsonParserDelegate
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.FIELD_NAME
import com.worldturner.medeia.parser.JsonTokenType.VALUE_NUMBER
import com.worldturner.medeia.parser.TOKEN_END_ARRAY
import com.worldturner.medeia.parser.TOKEN_END_OBJECT
import com.worldturner.medeia.parser.TOKEN_END_OF_STREAM
import com.worldturner.medeia.parser.TOKEN_FALSE
import com.worldturner.medeia.parser.TOKEN_NONE
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.TOKEN_START_ARRAY
import com.worldturner.medeia.parser.TOKEN_START_OBJECT
import com.worldturner.medeia.parser.TOKEN_TRUE
import com.worldturner.medeia.pointer.JsonPointer
import java.util.ArrayDeque

class JacksonTokenDataJsonParser(
    jsonParser: JsonParser,
    private val consumer: JsonTokenDataAndLocationConsumer,
    private val inputSourceName: String?
) : JsonParserDelegate(jsonParser), JsonParserAdapter {

    private var level: Int = 0
    private val dynamicTokenLocation = DynamicJsonTokenLocation()
    private val propertyNamesStack = ArrayDeque<MutableSet<String>>()

    override fun nextToken(): JsonToken? {
        val jacksonType = super.nextToken()
        val token = buildJsonTokenData(jacksonType)
        val type = token.type
        if (!type.syntheticType) {
            if (type.lastStructureToken)
                level--
            try {
                consumer.consume(token, dynamicTokenLocation)
            } finally {
                if (type.firstStructureToken)
                    level++
            }
        }
        when (jacksonType) {
            JsonToken.START_OBJECT -> propertyNamesStack.addFirst(HashSet())
            JsonToken.END_OBJECT -> propertyNamesStack.removeFirst()
            JsonToken.FIELD_NAME -> propertyNamesStack.peek() += currentName
            else -> {
            }
        }

        return jacksonType
    }

    private fun buildJsonTokenData(jacksonType: JsonToken?): JsonTokenData {
        return when (jacksonType) {
            null -> TOKEN_END_OF_STREAM
            JsonToken.VALUE_NUMBER_FLOAT, JsonToken.VALUE_NUMBER_INT -> {
                val numberType = numberType
                when (numberType) {
                    NumberType.INT, NumberType.LONG ->
                        JsonTokenData.createNumber(longValue)
                    NumberType.BIG_INTEGER ->
                        JsonTokenData(type = VALUE_NUMBER, integer = bigIntegerValue)
                    else ->
                        JsonTokenData(type = VALUE_NUMBER, decimal = decimalValue)
                }
            }
            JsonToken.FIELD_NAME ->
                JsonTokenData(type = FIELD_NAME, text = currentName)
            JsonToken.VALUE_STRING -> JsonTokenData.createText(text)
            JsonToken.VALUE_TRUE -> TOKEN_TRUE
            JsonToken.VALUE_FALSE -> TOKEN_FALSE
            JsonToken.START_OBJECT -> TOKEN_START_OBJECT
            JsonToken.END_OBJECT -> TOKEN_END_OBJECT
            JsonToken.START_ARRAY -> TOKEN_START_ARRAY
            JsonToken.END_ARRAY -> TOKEN_END_ARRAY
            JsonToken.VALUE_NULL -> TOKEN_NULL
            else -> TOKEN_NONE
        }
    }

    inner class DynamicJsonTokenLocation : JsonTokenLocation {
        override val pointer: JsonPointer
            get() = JsonPointer(parsingContext.pathAsPointer().toString(), bypassValidation = true)
        override val level: Int
            get() = this@JacksonTokenDataJsonParser.level

        override val line: Int
            get() = currentLocation.lineNr

        override val column: Int
            get() = currentLocation.columnNr

        override val propertyNames: Set<String>
            get() = propertyNamesStack.peek() ?: emptySet()

        override val inputSourceName: String?
            get() = this@JacksonTokenDataJsonParser.inputSourceName

        override fun toString(): String =
            inputSourceName?.let { "at $line:$column in $inputSourceName" } ?: "at $line:$column"
    }

    override fun parseAll() {
        while (nextToken() != null) {
        }
    }
}