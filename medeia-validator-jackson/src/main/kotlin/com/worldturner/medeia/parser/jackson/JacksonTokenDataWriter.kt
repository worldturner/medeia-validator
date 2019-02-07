package com.worldturner.medeia.parser.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
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

class JacksonTokenDataWriter(val generator: JsonGenerator) : JsonTokenDataConsumer {
    override fun consume(token: JsonTokenData) {
        when (token.type) {
            VALUE_NULL -> generator.writeNull()
            VALUE_TEXT -> generator.writeString(token.text)
            VALUE_BOOLEAN_FALSE -> generator.writeBoolean(false)
            VALUE_BOOLEAN_TRUE -> generator.writeBoolean(true)
            VALUE_NUMBER -> {
                if (token.hasLongValue()) {
                    generator.writeNumber(token.longValue)
                } else {
                    token.integer?.let {
                        generator.writeNumber(it)
                    } ?: token.decimal?.let {
                        generator.writeNumber(it)
                    }
                }
            }
            START_OBJECT -> generator.writeStartObject()
            END_OBJECT -> generator.writeEndObject()
            START_ARRAY -> generator.writeStartArray()
            END_ARRAY -> generator.writeEndArray()
            FIELD_NAME -> generator.writeFieldName(token.text)
            else -> {
            }
        }
    }
}