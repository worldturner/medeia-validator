package com.worldturner.medeia.parser.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.worldturner.medeia.parser.ArrayNodeData
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.NodeData
import com.worldturner.medeia.parser.ObjectNodeData
import com.worldturner.medeia.parser.TokenNodeData

class SimpleTreeSerializer : StdSerializer<NodeData>(NodeData::class.java) {
    override fun serialize(value: NodeData?, gen: JsonGenerator, provider: SerializerProvider?) {

        when (value) {
            is TokenNodeData -> {
                val token = value.token
                when (token.type) {
                    JsonTokenType.VALUE_NULL -> gen.writeNull()
                    JsonTokenType.VALUE_TEXT -> gen.writeString(token.text)
                    JsonTokenType.VALUE_BOOLEAN_FALSE -> gen.writeBoolean(false)
                    JsonTokenType.VALUE_BOOLEAN_TRUE -> gen.writeBoolean(true)
                    JsonTokenType.VALUE_NUMBER -> {
                        if (token.hasLongValue()) {
                            gen.writeNumber(token.longValue)
                        } else {
                            token.integer?.let { gen.writeNumber(it) } ?: token.decimal?.let { gen.writeNumber(it) }
                        }
                    }
                    else -> {
                    }
                }
            }
            is ArrayNodeData -> {
                gen.writeStartArray()
                value.nodes.forEach { serialize(it, gen, provider) }
                gen.writeEndArray()
            }
            is ObjectNodeData -> {
                gen.writeStartObject()
                value.nodes.forEach {
                    gen.writeFieldName(it.key)
                    serialize(it.value, gen, provider)
                }
                gen.writeEndObject()
            }
        }
    }
}