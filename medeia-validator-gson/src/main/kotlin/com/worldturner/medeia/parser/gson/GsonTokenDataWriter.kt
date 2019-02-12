package com.worldturner.medeia.parser.gson

import com.google.gson.stream.JsonWriter
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

class GsonTokenDataWriter(val writer: JsonWriter) : JsonTokenDataConsumer {
    override fun consume(token: JsonTokenData) {
        when (token.type) {
            VALUE_NULL -> writer.nullValue()
            VALUE_TEXT -> writer.value(token.text)
            VALUE_BOOLEAN_FALSE -> writer.value(false)
            VALUE_BOOLEAN_TRUE -> writer.value(true)
            VALUE_NUMBER -> {
                if (token.hasLongValue()) {
                    writer.value(token.longValue)
                } else {
                    token.integer?.let {
                        writer.value(it)
                    } ?: token.decimal?.let {
                        writer.value(it)
                    }
                }
            }
            START_OBJECT -> writer.beginObject()
            END_OBJECT -> writer.endObject()
            START_ARRAY -> writer.beginArray()
            END_ARRAY -> writer.endArray()
            FIELD_NAME -> writer.name(token.text)
            else -> {
            }
        }
    }
}