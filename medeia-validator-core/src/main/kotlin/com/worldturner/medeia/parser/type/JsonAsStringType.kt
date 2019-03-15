package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.TextOutputBuilder
import com.worldturner.medeia.parser.SimpleNode
import com.worldturner.medeia.parser.builder.ConsumerBuilderValueBuilder
import com.worldturner.medeia.parser.type.AcceptKind.SINGLE
import com.worldturner.medeia.parser.type.AcceptKind.STRUCTURE

object JsonAsStringType : StructuredType() {
    override fun accepts(token: JsonTokenData) =
        if (token.type.nonStructureToken) SINGLE else STRUCTURE

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Any? =
        SimpleNode(token).toString()

    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation) =
        ConsumerBuilderValueBuilder(location.level, TextOutputBuilder())

    override fun isComplete(token: JsonTokenData): Boolean = false

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        if (value == null) {
            consumer.consume(TOKEN_NULL)
        } else {
            // TODO: this writes the json not as raw text but as a text.
            // TODO: to write it out raw it needs to be parsed again
            consumer.consume(JsonTokenData.createText(value.toString()))
        }
    }
}