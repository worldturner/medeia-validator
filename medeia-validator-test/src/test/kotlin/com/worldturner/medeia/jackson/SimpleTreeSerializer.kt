package com.worldturner.medeia.parser.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.worldturner.medeia.parser.ArrayNode
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.TreeNode
import com.worldturner.medeia.parser.ObjectNode
import com.worldturner.medeia.parser.SimpleNode

class SimpleTreeSerializer : StdSerializer<TreeNode>(TreeNode::class.java) {
    override fun serialize(value: TreeNode?, gen: JsonGenerator, provider: SerializerProvider?) {

        when (value) {
            is SimpleNode -> {
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
            is ArrayNode -> {
                gen.writeStartArray()
                value.nodes.forEach { serialize(it, gen, provider) }
                gen.writeEndArray()
            }
            is ObjectNode -> {
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