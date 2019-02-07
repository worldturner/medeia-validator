package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.builder.ConsumerBuilderValueBuilder
import com.worldturner.medeia.parser.builder.IgnoreBuilder

object UnknownType : StructuredType() {
    override fun accepts(token: JsonTokenData) =
        if (token.type.nonStructureToken) AcceptKind.SINGLE else AcceptKind.STRUCTURE

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Any? = null
    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation) =
        ConsumerBuilderValueBuilder(location.level, IgnoreBuilder())

    override fun isComplete(token: JsonTokenData): Boolean = false

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        // No value was stored for this type
    }
}