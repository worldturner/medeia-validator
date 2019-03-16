package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.ArrayNode
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.ObjectNode
import com.worldturner.medeia.parser.SimpleTreeBuilder
import com.worldturner.medeia.parser.TOKEN_END_ARRAY
import com.worldturner.medeia.parser.TOKEN_END_OBJECT
import com.worldturner.medeia.parser.TOKEN_START_ARRAY
import com.worldturner.medeia.parser.TOKEN_START_OBJECT
import com.worldturner.medeia.parser.SimpleNode
import com.worldturner.medeia.parser.builder.ConsumerBuilderValueBuilder
import com.worldturner.medeia.parser.type.AcceptKind.SINGLE
import com.worldturner.medeia.parser.type.AcceptKind.STRUCTURE

object SimpleTreeType : StructuredType() {
    override fun accepts(token: JsonTokenData) =
        if (token.type.nonStructureToken) SINGLE else STRUCTURE

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Any? =
        SimpleNode(token)

    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation) =
        ConsumerBuilderValueBuilder(location.level, SimpleTreeBuilder(location.level))

    override fun isComplete(token: JsonTokenData): Boolean = false

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        when (value) {
            is SimpleNode -> {
                consumer.consume(value.token)
            }
            is ArrayNode -> {
                consumer.consume(TOKEN_START_ARRAY)
                value.nodes.forEach { write(it, consumer) }
                consumer.consume(TOKEN_END_ARRAY)
            }
            is ObjectNode -> {
                consumer.consume(TOKEN_START_OBJECT)
                value.nodes.forEach {
                    consumer.consume(JsonTokenData(type = JsonTokenType.FIELD_NAME, text = it.key))
                    write(it.value, consumer)
                }
                consumer.consume(TOKEN_END_OBJECT)
            }
        }
    }
}